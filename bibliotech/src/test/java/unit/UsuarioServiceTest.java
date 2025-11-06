package com.bibliotech.service;

import com.bibliotech.model.Usuario;
import com.bibliotech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setNome("Maria Silva");
        usuarioValido.setEmail("maria@teste.com");
        usuarioValido.setCpf("123.456.789-10");
        usuarioValido.setSenha("1234");
        usuarioValido.setAtivo(true);
    }

    @Test
    void deveValidarCpfCorreto() {
        assertTrue(usuarioService.validarCPF("123.456.789-10"));
    }

    @Test
    void deveInvalidarCpfComFormatoErrado() {
        assertFalse(usuarioService.validarCPF("12345678910"));
        assertFalse(usuarioService.validarCPF(""));
        assertFalse(usuarioService.validarCPF(null));
    }

    @Test
    void deveSalvarUsuarioComSucesso() {
        when(usuarioRepository.findByEmail(usuarioValido.getEmail()))
                .thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf(usuarioValido.getCpf()))
                .thenReturn(Optional.empty());
        when(usuarioRepository.save(usuarioValido))
                .thenReturn(usuarioValido);

        Usuario salvo = usuarioService.salvar(usuarioValido);

        assertNotNull(salvo);
        assertEquals("Maria Silva", salvo.getNome());
        verify(usuarioRepository, times(1)).save(usuarioValido);
    }

    @Test
    void deveLancarErroAoSalvarComCpfDuplicado() {
        when(usuarioRepository.findByCpf(usuarioValido.getCpf()))
                .thenReturn(Optional.of(usuarioValido));

        Usuario novo = new Usuario();
        novo.setEmail("novo@teste.com");
        novo.setCpf("123.456.789-10");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.salvar(novo));

        assertEquals("CPF já cadastrado", ex.getMessage());
    }

    @Test
    void deveAutenticarComSucesso() {
        when(usuarioRepository.findByEmail("maria@teste.com"))
                .thenReturn(Optional.of(usuarioValido));

        Optional<Usuario> resultado = usuarioService.autenticar("maria@teste.com", "1234");
        assertTrue(resultado.isPresent());
        assertEquals("maria@teste.com", resultado.get().getEmail());
    }

    @Test
    void deveRetornarListaDeUsuariosAtivos() {
        when(usuarioRepository.findByAtivoTrue())
                .thenReturn(List.of(usuarioValido));

        List<Usuario> ativos = usuarioService.listarAtivos();

        assertEquals(1, ativos.size());
        assertTrue(ativos.get(0).getAtivo());
    }

    @Test
    void deveExcluirUsuarioComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

        usuarioService.excluir(1L);

        verify(usuarioRepository, times(1)).delete(usuarioValido);
    }

    @Test
    void deveLancarErroAoExcluirUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.excluir(99L));

        assertEquals("Usuário não encontrado", ex.getMessage());
    }
}
