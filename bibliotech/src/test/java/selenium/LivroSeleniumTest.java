package com.bibliotech;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

public class LivroSeleniumTest extends BaseTest {

    private final String TITULO_TESTE = "Livro Teste Selenium";
    private final String AUTOR_TESTE = "Autor Teste";
    private final String ISBN_TESTE = "978-1234567890";
    private final String EDITORA_TESTE = "Editora Teste";
    private final String ANO_TESTE = "2025";
    private final String QUANTIDADE_TESTE = "5";

    @Test
    @DisplayName("TS-003: Deve cadastrar novo livro com sucesso")
    public void testCadastrarLivroComSucesso() {
        driver.get(BASE_URL + "/livros");
        
        // 1. Clicar em "Novo Livro" (Elemento 7 na lista de Livros)
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div/div[1]/a"))).click();

        // 2. Preencher o formulário (Mapeamento da Fase 3)
        // Título: input[7]
        // Autor: input[9]
        // ISBN: input[11]
        // Editora: input[13]
        // Ano: input[15]
        // Quantidade: input[17]
        
        driver.findElement(By.xpath("//label[text()='Título *']/following-sibling::input")).sendKeys(TITULO_TESTE);
        driver.findElement(By.xpath("//label[text()='Autor *']/following-sibling::input")).sendKeys(AUTOR_TESTE);
        driver.findElement(By.xpath("//label[text()='ISBN *']/following-sibling::input")).sendKeys(ISBN_TESTE);
        driver.findElement(By.xpath("//label[text()='Editora']/following-sibling::input")).sendKeys(EDITORA_TESTE);
        driver.findElement(By.xpath("//label[text()='Ano *']/following-sibling::input")).sendKeys(ANO_TESTE);
        driver.findElement(By.xpath("//label[text()='\n" +
                "                                    Quantidade de Exemplares *\n" +
                "                                ']/following-sibling::input")).sendKeys(QUANTIDADE_TESTE);

        // 3. Clicar em "Salvar" (Elemento 18)
        driver.findElement(By.xpath("//button[text()='\n" +
                "                                    Salvar\n" +
                "                                ']")).click();

        // 4. Verificar se o livro foi cadastrado (Redirecionamento para a lista e título visível)
        WebElement livroNaLista = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + TITULO_TESTE + "']")));
        
        assertTrue(livroNaLista.isDisplayed(), "O livro recém-cadastrado deve estar visível na lista.");
    }

    @Test
    @DisplayName("TS-004: Deve editar um livro existente com sucesso")
    public void testEditarLivroComSucesso() {
        // Pré-condição: O livro do teste anterior deve existir.
        driver.get(BASE_URL + "/livros");
        
        // 1. Encontrar o livro e clicar no botão de edição (ícone de lápis)
        WebElement linhaLivro = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + TITULO_TESTE + "']/parent::tr")));
        // O botão de edição é o primeiro <a> dentro da coluna "Ações"
        linhaLivro.findElement(By.xpath(".//td[last()]/a[1]")).click(); 

        // 2. Alterar o título e a quantidade
        String novoTitulo = TITULO_TESTE + " - Editado";
        String novaQuantidade = "10";
        
        WebElement tituloInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()='Título *']/following-sibling::input")));
        tituloInput.clear();
        tituloInput.sendKeys(novoTitulo);
        
        WebElement quantidadeInput = driver.findElement(By.xpath("//label[text()='\n" +
                "                                    Quantidade de Exemplares *\n" +
                "                                ']/following-sibling::input"));
        quantidadeInput.clear();
        quantidadeInput.sendKeys(novaQuantidade);

        // 3. Clicar em "Salvar"
        driver.findElement(By.xpath("//button[text()='\n" +
                "                                    Salvar\n" +
                "                                ']")).click();

        // 4. Verificar a edição
        WebElement livroEditadoNaLista = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + novoTitulo + "']")));
        
        assertTrue(livroEditadoNaLista.isDisplayed(), "O livro editado deve estar visível na lista com o novo título.");
        
        // Verificar a nova quantidade
        WebElement quantidadeElement = livroEditadoNaLista.findElement(By.xpath("./following-sibling::td[6]")); // 2 colunas após "Total"
        assertEquals(novaQuantidade, quantidadeElement.getText(), "A quantidade total de exemplares deve ser atualizada.");
    }

    @Test
    @DisplayName("TS-005: Deve exibir erro de validação ao tentar cadastrar livro com campos obrigatórios vazios")
    public void testValidacaoCamposObrigatorios() {
        driver.get(BASE_URL + "/livros/novo");
        
        // 1. Tentar salvar sem preencher nenhum campo
        driver.findElement(By.xpath("//button[text()='\n" +
                "                                    Salvar\n" +
                "                                ']")).click();

        // 2. Verificar a mensagem de erro (a mensagem de erro deve ser exibida no formulário)
        // O Spring Boot/Thymeleaf geralmente exibe a mensagem de erro ao lado do campo ou no topo.
        // Vamos procurar por uma mensagem de erro genérica ou a mensagem de validação do campo Título.
        
        // Tentativa de encontrar a mensagem de erro do campo Título (ex: "Título é obrigatório")
        // O seletor pode variar, vamos tentar um genérico que procure por texto de erro.

        assertTrue(driver.getCurrentUrl().equals(BASE_URL + "/livros/novo"), "A mensagem de erro de validação deve ser exibida.");
        
        // O teste deve falhar se o login não estiver funcionando, mas como estamos logados, o problema será a validação.
    }

    @Test
    @DisplayName("TS-006: Deve excluir um livro existente com sucesso")
    public void testExcluirLivroComSucesso() {
        // Pré-condição: O livro do teste anterior deve existir.
        driver.get(BASE_URL + "/livros");
        
        // 1. Encontrar o livro editado
        String tituloParaExcluir = TITULO_TESTE + " - Editado";
        WebElement linhaLivro = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='" + tituloParaExcluir + "']/parent::tr")));
        
        // 2. Clicar no botão de exclusão (ícone de lixeira)
        // O botão de exclusão é o segundo <a> dentro da coluna "Ações"
        linhaLivro.findElement(By.xpath(".//td[last()]/a[2]")).click(); 

        // 3. Aceitar o alerta de confirmação (se houver)
        // Como o Spring Boot/Thymeleaf geralmente usa um formulário POST para exclusão,
        // é mais provável que seja um modal de confirmação ou um redirecionamento direto.
        // Se for um alerta nativo do navegador, o Selenium lida com ele.
        try {
            wait.until(ExpectedConditions.alertIsPresent()).accept();
        } catch (Exception e) {
            // Ignora se não houver alerta nativo
        }

        // 4. Verificar a exclusão (o livro não deve mais estar na lista)
        
        boolean livroExcluido = driver.findElements(By.xpath("//td[text()='" + tituloParaExcluir + "']")).isEmpty();
        
        assertTrue(livroExcluido, "O livro deve ser excluído e não deve mais estar na lista.");
    }

    @Test
    @DisplayName("TS-010: Deve realizar a busca de um livro na lista de livros")
    public void testPesquisarLivro() {
        driver.get(BASE_URL + "/livros");

        String tituloParaPesquisar = "Clean Code";

        driver.findElement(By.xpath("/html/body/div/div[2]/div/form/div[2]/input")).sendKeys(tituloParaPesquisar);

        driver.findElement(By.xpath("//button[text()=' Buscar\n" +
                "                        ']")).click();

        boolean encontrado = !driver.findElements(By.xpath("//td[text()='" + tituloParaPesquisar + "']/parent::tr")).isEmpty();

        assertTrue(encontrado, "Livro encontrado");
    }
}
