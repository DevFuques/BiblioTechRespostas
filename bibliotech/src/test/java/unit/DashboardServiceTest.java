package com.bibliotech.service;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private List<Emprestimo> emprestimosAtrasados;
    private List<Object[]> livrosMaisEmprestados;

    @BeforeEach
    void setUp() {
        // Configuração para empréstimos atrasados
        emprestimosAtrasados = Arrays.asList(
            new Emprestimo(), 
            new Emprestimo()
        );
        
        // Configuração para livros mais emprestados
        Livro livro1 = new Livro();
        livro1.setTitulo("Livro Popular 1");
        
        Livro livro2 = new Livro();
        livro2.setTitulo("Livro Popular 2");
        
        livrosMaisEmprestados = Arrays.asList(
            new Object[]{livro1, 10L},
            new Object[]{livro2, 5L}
        );
    }

    @Test
    void testObterEstatisticas() {
        // Arrange
        when(livroRepository.count()).thenReturn(100L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(Collections.nCopies(50, new Usuario()));
        when(emprestimoRepository.count()).thenReturn(30L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(Collections.nCopies(80, new Livro()));
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(emprestimosAtrasados);
        
        // Act
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();
        
        // Assert
        assertEquals(100L, estatisticas.get("totalLivros"));
        assertEquals(50L, estatisticas.get("totalUsuarios"));
        assertEquals(30L, estatisticas.get("emprestimosAtivos"));
        assertEquals(80L, estatisticas.get("livrosDisponiveis"));
        assertEquals(2L, estatisticas.get("emprestimosAtrasados"));
    }

    @Test
    void testObterLivrosMaisEmprestados() {
        // Arrange
        when(emprestimoRepository.findLivrosMaisEmprestados()).thenReturn(livrosMaisEmprestados);
        
        // Act
        List<Object[]> resultado = dashboardService.obterLivrosMaisEmprestados();
        
        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Livro Popular 1", ((Livro)resultado.get(0)[0]).getTitulo());
        assertEquals(10L, resultado.get(0)[1]);
    }

    // Removido método duplicado testObterEmprestimosAtrasados

    
    @Test
    void testObterLivrosMaisEmprestadosVazio() {
        // Arrange
        when(emprestimoRepository.findLivrosMaisEmprestados()).thenReturn(Arrays.asList());

        // Act
        List<Object[]> resultado = dashboardService.obterLivrosMaisEmprestados();

        // Assert
        assertEquals(0, resultado.size());
    }

    @Test
    void testObterEstatisticasZeros() {
        // Arrange
        when(livroRepository.count()).thenReturn(0L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(Collections.emptyList());
        when(emprestimoRepository.count()).thenReturn(0L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(Collections.emptyList());
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();

        // Assert
        assertEquals(0L, estatisticas.get("totalLivros"));
        assertEquals(0L, estatisticas.get("totalUsuarios"));
        assertEquals(0L, estatisticas.get("emprestimosAtivos"));
        assertEquals(0L, estatisticas.get("livrosDisponiveis"));
        assertEquals(0L, estatisticas.get("emprestimosAtrasados"));
    }

    @Test
    void testEmprestimosAtivosDeveriaConsiderarApenasAtivos() {
        // Arrange: cenário em que existem 20 ativos e 30 no total
        when(livroRepository.count()).thenReturn(100L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(Collections.nCopies(50, new Usuario()));
        when(emprestimoRepository.count()).thenReturn(30L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(Collections.nCopies(80, new Livro()));
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(emprestimosAtrasados);

        // Act
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();

        // Assert - Deve falhar: serviço usa count() geral em vez de apenas ativos
        assertEquals(20L, estatisticas.get("emprestimosAtivos"), "RN-DASH: deveria contar apenas empréstimos ativos");
    }
    @Test
    void testObterEmprestimosAtrasados() {
        // Arrange
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(emprestimosAtrasados);
        
        // Act
        List<Emprestimo> resultado = dashboardService.obterEmprestimosAtrasados();
        
        // Assert
        assertEquals(2, resultado.size());
        verify(emprestimoRepository, times(1)).findEmprestimosAtrasados();
    }

    @Test
    void testContarLivrosDisponiveis() {
        // Arrange
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(Collections.nCopies(80, new Livro()));
        
        // Act
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();
        
        // Assert
        assertEquals(80L, estatisticas.get("livrosDisponiveis"));
    }
    
    @Test
    void testEstatisticasComValorErrado() {
        // Arrange
        when(livroRepository.count()).thenReturn(100L);
        when(usuarioRepository.findByAtivoTrue()).thenReturn(Collections.nCopies(50, new Usuario()));
        when(emprestimoRepository.count()).thenReturn(30L);
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0)).thenReturn(Collections.nCopies(80, new Livro()));
        when(emprestimoRepository.findEmprestimosAtrasados()).thenReturn(emprestimosAtrasados);
        
        // Act
        Map<String, Object> estatisticas = dashboardService.obterEstatisticas();
        
        // Assert - Este teste vai falhar propositalmente
        // O valor correto seria 100L
        assertEquals(150L, estatisticas.get("totalLivros"), "Este teste deve falhar pois o valor esperado está incorreto");
    }
    
    
}