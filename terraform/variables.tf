variable "aws_region" {
  description = "Região AWS para implantar os recursos"
  type        = string
  default     = "us-east-1"
}

variable "github_owner" {
  description = "Nome do proprietário da organização ou usuário do GitHub"
  type        = string
  default     = "Grupo-110-Fiap"
}

variable "project_name" {
  description = "Nome do projeto para tagging e prefixos de recursos"
  type        = string
  default     = "tech-challenge"
}

variable "lambda_s3_bucket" {
  description = "Nome do bucket S3 onde o pacote da Lambda será armazenado"
  type        = string
  default     = "grupo-125-tech-challenge-lambda"
}

variable "lambda_s3_key" {
  description = "Chave do objeto do pacote da Lambda no S3"
  type        = string
  default = "lambda-tech-challenge.jar"
}

variable "lambda_handler_name" {
  description = "Nome do handler da função Lambda"
  type        = string
  default     = "br.com.fiap.techchallenge.lambda.LambdaHandler::handleRequest"
}

variable "lambda_runtime" {
  description = "Runtime da função Lambda"
  type        = string
  default     = "java21"
}

variable "cognito_secret_name" {
  type    = string
  default = "tech-challenge/cognito/config"
}