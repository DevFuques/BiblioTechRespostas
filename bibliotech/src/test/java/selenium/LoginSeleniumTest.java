package com.bibliotech.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginSeleniumTest {

    private static WebDriver driver;

    @BeforeAll
    public static void setup() {
        driver = WebDriverUtil.getDriver();
        WebDriverUtil.openBaseUrl();
    }

    @Test
    @Order(1)
    public void TS_001_loginValido() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.id("btnLogin")).click();

        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
            "Usuário deveria ser redirecionado ao dashboard após login válido");
    }

    @Test
    @Order(2)
    public void TS_002_loginInvalido() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("senhaErrada");
        driver.findElement(By.id("btnLogin")).click();

        String msgErro = driver.findElement(By.id("error-message")).getText();
        Assertions.assertTrue(msgErro.contains("senha inválida"),
            "Mensagem de erro esperada ao tentar login com senha incorreta");
    }

    @AfterAll
    public static void tearDown() {
        WebDriverUtil.closeDriver();
    }
}
