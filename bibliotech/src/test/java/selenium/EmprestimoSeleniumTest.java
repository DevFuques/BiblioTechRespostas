package com.bibliotech;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.*;

public class EmprestimoSeleniumTest extends BaseTest {

    private final String USUARIO_TESTE = "João Silva";
    private final String LIVRO_TESTE = "Clean Code";

    @Test
    @DisplayName("TS-007: Deve realizar um novo empréstimo com sucesso")
    public void testRealizarEmprestimoComSucesso() {
        driver.get(BASE_URL + "/emprestimos");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()=' Novo Empréstimo\n" +
                "            ']"))).click();

        WebElement selectUsuario = driver.findElement(By.xpath("//select"));
        Select usuarioSelect = new Select(selectUsuario);
        usuarioSelect.selectByVisibleText(USUARIO_TESTE + " (ALUNO)");

        WebElement selectLivro = driver.findElement(By.xpath("(//select)[2]"));
        Select livroSelect = new Select(selectLivro);
        livroSelect.selectByIndex(1);

        driver.findElement(By.xpath("//button[text()='\n" +
                "                                    Realizar Empréstimo\n" +
                "                                ']")).click();

        WebElement emprestimoNaLista = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + USUARIO_TESTE + "']/parent::tr")));
        
        assertTrue(emprestimoNaLista.isDisplayed(), "O novo empréstimo deve estar visível na lista.");

        WebElement statusElement = emprestimoNaLista.findElement(By.xpath(".//span[text()='Ativo']"));
        assertTrue(statusElement.isDisplayed(), "O status do empréstimo deve ser 'Ativo'.");
    }

    @Test
    @DisplayName("TS-008: Deve realizar a devolução de um livro com sucesso")
    public void testRealizarDevolucaoComSucesso() {
        driver.get(BASE_URL + "/emprestimos");

        WebElement linhaEmprestimo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + USUARIO_TESTE + "']/parent::tr")));

        linhaEmprestimo.findElement(By.xpath(".//td[last()]/a[1]")).click();

        WebElement linhaEmprestimoDevolvido = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + USUARIO_TESTE + "']/parent::tr")));

        WebElement statusElement = linhaEmprestimoDevolvido.findElement(By.xpath(".//span[text()='Devolvido']"));
        assertTrue(statusElement.isDisplayed(), "O status do empréstimo deve ser 'Devolvido' após a devolução.");
    }

    @Test
    @DisplayName("TS-009: Deve exibir erro de validação ao tentar realizar empréstimo sem selecionar usuário/livro")
    public void testValidacaoEmprestimoCamposObrigatorios() {
        driver.get(BASE_URL + "/emprestimos/novo");

        driver.findElement(By.xpath("//button[text()='\n" +
                "                                    Realizar Empréstimo\n" +
                "                                ']")).click();

        assertTrue(driver.getCurrentUrl().equals(BASE_URL + "/emprestimos/novo"), "A mensagem de erro de validação deve ser exibida.");
    }
}
