# Tech Challenge Lambda Project

Este projeto implementa um serviço de autenticação de usuários utilizando AWS Lambda e Cognito. A aplicação é construída em Kotlin, gerenciada com Gradle e a infraestrutura é provisionada utilizando Terraform. O deploy é automatizado com GitHub Actions.

## Tech stack

- Kotlin 1.9 on Java 21
- Build: Gradle 8+
- Infra: Terraform
- Deploy: GitHub Actions
- AWS Lambda

## Funcionalidades
- Registro de Usuários: Endpoint para registrar novos usuários no AWS Cognito.
- Login de Usuários: Endpoint para autenticar usuários existentes e retornar um token de acesso. 
- Autenticação Anônima: Endpoint para obter um token de acesso para um usuário anônimo pré-configurado. 
- Infraestrutura como Código: Utilização de Terraform para provisionar todos os recursos da AWS de forma automatizada e replicável. 
- Deploy Automatizado: Workflows de GitHub Actions para build, teste e deploy da aplicação.

## Pré-requisitos
- [Java 21](https://adoptium.net/) e [Gradle 8+](https://gradle.org/install/) instalados.
- [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) instalado para provisionar a infraestrutura.
- Conta na [AWS](https://aws.amazon.com/) com permissões
- [AWS CLI](https://aws.amazon.com/cli/) configurado com as credenciais apropriadas.

## Como começar

1. Clone o repositório:
   ```bash
   git clone git@github.com:Grupo-110-Fiap/tech-challenge-lambda.git
   cd lambda-tech-challenge

2. Configurar as Credenciais da AWS
    Configure suas credenciais da AWS no ambiente em que você irá executar o Terraform e o deploy. Você pode fazer isso através de variáveis de ambiente ou do arquivo ~/.aws/credentials.
3. Provisionar a Infraestrutura com Terraform
   Navegue até o diretório terraform e execute os seguintes comandos:
    ```bash
   cd terraform
    terraform init
    terraform plan
    terraform apply
   
4. Fazer o Build da Aplicação
    Na raiz do projeto, execute o seguinte comando para gerar o arquivo JAR da aplicação:
    ```bash
   ./gradlew shadowJar
5. Fazer o Deploy
    
    O deploy é feito através dos workflows do GitHub Actions. Ao fazer um push para a branch main, o workflow deploy.yml será acionado, fazendo o build da aplicação e o deploy na AWS.

6. Eventos do Lambda

    A aplicação é acionada por eventos HTTP através do API Gateway. Você pode testar os endpoints utilizando ferramentas como Postman ou curl. 

   `/register`: Registra um novo usuário. O corpo da requisição deve conter cpf, email e nome.

   `/login`: Autentica um usuário existente. O corpo da requisição deve conter o cpf do usuário.
   
   `/anonymous`: Autentica um usuário anônimo. O corpo da requisição não precisa de dado algum.

## Testes

### Testes locais com SAM CLI
O projeto inclui configuração para testes locais utilizando AWS SAM CLI, permitindo executar e debugar a função Lambda em ambiente local antes do deploy.
Pré-requisitos para Testes Locais

Certifique-se de que você tenha:
- AWS SAM CLI instalado 
- Docker instalado e em execução (necessário para o SAM)
- Build da aplicação realizado 
  ```bash 
  ./gradlew shadowJar
  ```
###

1. Executando a API Localmente:

    Para iniciar a API Gateway localmente com debug habilitado:
    ```bash
    sam local start-api -p 3000 --debug-port 5005
    ```
   Este comando irá:
    - Iniciar um servidor local na porta 3000 (padrão)
    - Expor a porta 5005 para debug remoto 
    - Simular o ambiente API Gateway e Lambda


2. Testando com eventos pré-configurados 
    O projeto inclui arquivos de eventos de teste que podem ser utilizados para invocar a função Lambda diretamente:
    - Teste de Registro de Usuário 
        ```bash
        sam local invoke TechChallengeLambdaFunction --event event-test-register.json --debug-port 5005
        ```
    - Teste de Login de Usuário
        ```bash
        sam local invoke TechChallengeLambdaFunction --event event-test-login.json --debug-port 5005
        ```
    - Teste de Usuário Anônimo
        ```bash
        sam local invoke TechChallengeLambdaFunction --event event-test-login-anonymous.json --debug-port 5005
        ```

3. Arquivos de Eventos de Teste

    O projeto inclui os seguintes arquivos de eventos pré-configurados:

   | Arquivo                         | Descrição                                          |
   |---------------------------------|----------------------------------------------------|
   | event-test-register.json        | Evento para testar o registro de um novo usuário   |
   | event-test-login.json           | Evento para testar o login de um usuário existente |
   | event-test-login-anonymous.json | Evento para testar o login anônimo                 |
    
4. Debug Remoto

   Quando se utiliza a opção `--debug-port 5005`, pode conectar um debugger remoto (como o IntelliJ IDEA ou VS Code) na porta 5005 para fazer debug do código Kotlin em tempo real. 
   
   Configuração no IntelliJ IDEA
   1. Vá em Run → Edit Configurations
   2. Clique em + e selecione Remote JVM Debug
   3. Configure:
      - `Host: localhost `
      - `Port: 5005 `
      - `Use module classpath: selecione o módulo do projeto`
   4. Execute o debugger após iniciar o SAM local










