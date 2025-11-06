package com.bibliotech.service;

import com.bibliotech.model.Livro;
import com.bibliotech.model.Emprestimo;
import com.bibliotech.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroService livroService;

    private Livro livro;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");
        livro.setAutor("Robert C. Martin");
        livro.setIsbn("978-0132350884");
        livro.setQuantidadeExemplares(5);
        livro.setQuantidadeDisponivel(5);
        livro.setEmprestimos(new ArrayList<>());
    }

    @Test
    void deveDefinirQuantidadeDisponivelQuandoNull() {
        livro.setQuantidadeDisponivel(null);
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro salvo = livroService.salvar(livro);

        assertEquals(5, salvo.getQuantidadeDisponivel());
        verify(livroRepository).save(livro);
    }

    @Test
    void deveManterQuantidadeDisponivelSeJaDefinida() {
        livro.setQuantidadeDisponivel(3);
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro salvo = livroService.salvar(livro);

        assertEquals(3, salvo.getQuantidadeDisponivel());
        verify(livroRepository).save(livro);
    }

    @Test
    void deveListarTodosOsLivros() {
        when(livroRepository.findAll()).thenReturn(List.of(livro));

        List<Livro> resultado = livroService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Clean Code", resultado.get(0).getTitulo());
    }

    @Test
    void deveBuscarPorIdExistente() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        Optional<Livro> resultado = livroService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Clean Code", resultado.get().getTitulo());
    }

    @Test
    void deveRetornarEmptyQuandoIdNaoExistir() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Livro> resultado = livroService.buscarPorId(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarPorIsbn() {
        when(livroRepository.findByIsbn("978-0132350884")).thenReturn(Optional.of(livro));

        Optional<Livro> resultado = livroService.buscarPorIsbn("978-0132350884");

        assertTrue(resultado.isPresent());
        assertEquals("Clean Code", resultado.get().getTitulo());
    }

    @Test
    void deveListarApenasLivrosDisponiveis() {
        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0))
                .thenReturn(List.of(livro));

        List<Livro> disponiveis = livroService.listarDisponiveis();

        assertEquals(1, disponiveis.size());
        assertEquals("Clean Code", disponiveis.get(0).getTitulo());
    }

    @Test
    void deveExcluirLivroSemEmprestimosAtivos() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        livroService.excluir(1L);

        verify(livroRepository).delete(livro);
    }

    @Test
    void deveLancarErroAoExcluirLivroComEmprestimosAtivos() {
        Emprestimo emprestimoAtivo = new Emprestimo();
        emprestimoAtivo.setAtivo(true);
        livro.getEmprestimos().add(emprestimoAtivo);

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.excluir(1L));

        assertEquals("Não é possível excluir livro com empréstimos ativos", ex.getMessage());
        verify(livroRepository, never()).delete(any());
    }

    @Test
    void deveLancarErroAoExcluirLivroInexistente() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.excluir(99L));

        assertEquals("Livro não encontrado", ex.getMessage());
    }

    @Test
    void deveIncrementarDisponibilidade() {
        livro.setQuantidadeDisponivel(2);

        livroService.incrementarDisponibilidade(livro);

        assertEquals(3, livro.getQuantidadeDisponivel());
        verify(livroRepository).save(livro);
    }

    @Test
    void deveDecrementarDisponibilidadeQuandoMaiorQueZero() {
        livro.setQuantidadeDisponivel(2);

        livroService.decrementarDisponibilidade(livro);

        assertEquals(1, livro.getQuantidadeDisponivel());
        verify(livroRepository).save(livro);
    }

    @Test
    void naoDeveDecrementarQuandoDisponibilidadeZero() {
        livro.setQuantidadeDisponivel(0);

        livroService.decrementarDisponibilidade(livro);

        assertEquals(0, livro.getQuantidadeDisponivel());
        verify(livroRepository, never()).save(any());
    }
}
