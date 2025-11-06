package com.bibliotech.service;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroService livroService;

    @InjectMocks
    private EmprestimoService emprestimoService;

    private Usuario usuario;
    private Livro livro;
    private Emprestimo emprestimo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário Teste");

        livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Livro Teste");
        livro.setQuantidadeDisponivel(5);

        emprestimo = new Emprestimo();
        emprestimo.setId(1L);
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(7));
        emprestimo.setAtivo(true);
    }

    @Test
    void testRealizarEmprestimo() {
        // Arrange
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);
        doNothing().when(livroService).decrementarDisponibilidade(livro);

        // Act
        Emprestimo resultado = emprestimoService.realizarEmprestimo(usuario, livro);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertEquals(livro, resultado.getLivro());
        assertEquals(LocalDate.now(), resultado.getDataEmprestimo());
        assertEquals(LocalDate.now().plusDays(7), resultado.getDataDevolucaoPrevista());
        assertTrue(resultado.getAtivo());
        verify(livroService, times(1)).decrementarDisponibilidade(livro);
    }

    @Test
    void testCalcularMultaComAtraso() {
        // Arrange
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(3));
        
        // Act
        double multa = emprestimoService.calcularMulta(emprestimo);
        
        // Assert
        assertEquals(9.0, multa); // 3 dias * R$3,00
    }

    @Test
    void testRegistrarDevolucao() {
        // Arrange
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(2));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);
        doNothing().when(livroService).incrementarDisponibilidade(livro);
        
        // Act
        Emprestimo resultado = emprestimoService.registrarDevolucao(1L);
        
        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getDataDevolucaoReal());
        assertFalse(resultado.getAtivo());
        assertEquals(6.0, resultado.getMulta()); // 2 dias * R$3,00
        verify(livroService, times(1)).incrementarDisponibilidade(livro);
    }

    @Test
    void testIsAtrasado() {
        // Arrange
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(1));
        
        // Act & Assert
        assertTrue(emprestimo.isAtrasado());
        
        // Caso com devolução já realizada
        emprestimo.setDataDevolucaoReal(LocalDate.now());
        assertFalse(emprestimo.isAtrasado());
    }
    
    @Test
    void testCalculoMultaErrado() {
        // Arrange
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(3));
        
        // Act
        double multa = emprestimoService.calcularMulta(emprestimo);
        
        // Assert - Este teste vai falhar propositalmente
        // O valor correto seria 9.0 (3 dias * R$3,00)
        assertEquals(6.0, multa, "Este teste deve falhar pois o valor esperado está incorreto");
    }
    
    @Test
    void testCalcularDataDevolucaoDeveSer14Dias() {
        // Arrange
        LocalDate dataEmprestimo = LocalDate.of(2025, 1, 1);

        // Act
        LocalDate prevista = emprestimoService.calcularDataDevolucao(dataEmprestimo);

        // Assert - Deve falhar, serviço está retornando 7 dias
        assertEquals(dataEmprestimo.plusDays(14), prevista, "RN-01: prazo padrão deve ser 14 dias");
    }

    @Test
    void testCalcularMultaRegraDeNegocioDoisReaisPorDia() {
        // Arrange: atraso de 2 dias
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().minusDays(2));
        emprestimo.setDataDevolucaoReal(null);

        // Act
        double multa = emprestimoService.calcularMulta(emprestimo);

        // Assert - Deve falhar, serviço está calculando R$3,00/dia
        assertEquals(4.0, multa, 0.0001, "RN-03: multa deve ser R$2,00 por dia de atraso");
    }

    @Test
    void testRealizarEmprestimoLivroIndisponivel() {
        // Arrange: livro sem disponibilidade
        livro.setQuantidadeDisponivel(0);
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);
        doNothing().when(livroService).decrementarDisponibilidade(livro);

        // Act & Assert - Deve falhar, serviço não valida disponibilidade
        assertThrows(RuntimeException.class,
            () -> emprestimoService.realizarEmprestimo(usuario, livro),
            "RN-02: deveria lançar exceção se o livro não estiver disponível");
    }

    @Test
    void testCalcularMultaSemAtraso() {
        // Arrange: devolução ainda dentro do prazo
        emprestimo.setDataDevolucaoPrevista(LocalDate.now().plusDays(3));
        emprestimo.setDataDevolucaoReal(null);

        // Act
        double multa = emprestimoService.calcularMulta(emprestimo);

        // Assert
        assertEquals(0.0, multa, 0.0001);
    }

    @Test
    void testCalcularMultaComDataRealSemAtraso() {
        // Arrange: devolvido na data prevista
        LocalDate hoje = LocalDate.now();
        emprestimo.setDataDevolucaoPrevista(hoje);
        emprestimo.setDataDevolucaoReal(hoje);

        // Act
        double multa = emprestimoService.calcularMulta(emprestimo);

        // Assert
        assertEquals(0.0, multa, 0.0001);
    }


}