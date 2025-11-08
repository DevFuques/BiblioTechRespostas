package com.bibliotech.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioSeleniumTest {

    private static WebDriver driver;

    @BeforeAll
    public static void setup() {
        driver = WebDriverUtil.getDriver();
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.id("btnLogin")).click();
    }

    @Test
    @Order(1)
    public void TS_003_cadastrarUsuario() {
        driver.get("http://localhost:8080/usuarios/novo");
        driver.findElement(By.id("nome")).sendKeys("João Teste");
        driver.findElement(By.id("email")).sendKeys("joao@teste.com");
        driver.findElement(By.id("senha")).sendKeys("123456");
        driver.findElement(By.id("btnSalvar")).click();

        String sucesso = driver.findElement(By.id("msgSucesso")).getText();
        Assertions.assertTrue(sucesso.contains("cadastrado com sucesso"));
    }

    @Test
    @Order(2)
    public void TS_004_editarUsuario() {
        driver.get("http://localhost:8080/usuarios");
        driver.findElement(By.xpath("//td[contains(text(),'João Teste')]/..//a[text()='Editar']")).click();
        driver.findElement(By.id("nome")).clear();
        driver.findElement(By.id("nome")).sendKeys("João Teste Editado");
        driver.findElement(By.id("btnSalvar")).click();

        String sucesso = driver.findElement(By.id("msgSucesso")).getText();
        Assertions.assertTrue(sucesso.contains("editado com sucesso"));
    }

    @Test
    @Order(3)
    public void TS_005_validacaoCamposVazios() {
        driver.get("http://localhost:8080/usuarios/novo");
        driver.findElement(By.id("btnSalvar")).click();

        String erro = driver.findElement(By.id("msgErro")).getText();
        Assertions.assertTrue(erro.contains("campos obrigatórios"));
    }

    @Test
    @Order(4)
    public void TS_006_excluirUsuario() {
        driver.get("http://localhost:8080/usuarios");
        driver.findElement(By.xpath("//td[contains(text(),'João Teste Editado')]/..//a[text()='Excluir']")).click();
        driver.switchTo().alert().accept();

        String sucesso = driver.findElement(By.id("msgSucesso")).getText();
        Assertions.assertTrue(sucesso.contains("excluído com sucesso"));
    }

    @AfterAll
    public static void tearDown() {
        WebDriverUtil.closeDriver();
    }
}
