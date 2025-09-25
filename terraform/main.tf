terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

data "aws_caller_identity" "current" {}

# Recurso IAM Role para a função Lambda
resource "aws_iam_role" "lambda_exec_role" {
  name = "${var.project_name}-lambda-exec-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    Project = var.project_name
  }
}

resource "aws_iam_role_policy_attachment" "lambda_basic_execution_policy" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "lambda_tech_challenge" {
  function_name    = "${var.project_name}-kotlin-lambda"
  handler          = var.lambda_handler_name
  runtime          = var.lambda_runtime
  role             = aws_iam_role.lambda_exec_role.arn
  s3_bucket        = var.lambda_s3_bucket_name
  s3_key           = var.lambda_s3_key
  source_code_hash = filebase64sha256("${path.module}/../build/libs/tech-challenge-lambda.jar")

  environment {
    variables = {
      REGION = var.aws_region
    }
  }

  tags = {
    Project = var.project_name
    Environment = "Development"
  }
}

resource "aws_api_gateway_rest_api" "tech_challenge_api" {
  name        = "${var.project_name}-api"
  description = "tech-challenge API Gateway"

  tags = {
    Project = var.project_name
    Environment = "Development"
  }
}

resource "aws_api_gateway_resource" "my_resource" { # TODO mudar esse my_resource
  rest_api_id = aws_api_gateway_rest_api.tech_challenge_api.id
  parent_id   = aws_api_gateway_rest_api.tech_challenge_api.root_resource_id
  path_part   = "hello"
}

resource "aws_api_gateway_method" "my_method" { # TODO mudar esse my_method
  rest_api_id   = aws_api_gateway_rest_api.tech_challenge_api.id
  resource_id   = aws_api_gateway_resource.my_resource.id
  http_method   = "GET"
  authorization = "NONE" # Sem autenticação para este endpoint inicial
}

resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id             = aws_api_gateway_rest_api.tech_challenge_api.id
  resource_id             = aws_api_gateway_resource.my_resource.id
  http_method             = aws_api_gateway_method.my_method.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda_tech_challenge.invoke_arn
}

resource "aws_lambda_permission" "allow_api_gateway_invoke_lambda" {
  statement_id  = "AllowAPIGatewayInvokeLambda"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_tech_challenge.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.tech_challenge_api.execution_arn}/*/*"
}

resource "aws_api_gateway_deployment" "api_deployment" {
  depends_on = [
    aws_api_gateway_method.my_method,
    aws_api_gateway_integration.lambda_integration,
  ]

  rest_api_id = aws_api_gateway_rest_api.tech_challenge_api.id
}

resource "aws_api_gateway_stage" "dev" {
  deployment_id = aws_api_gateway_deployment.api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.tech_challenge_api.id
  stage_name    = "dev"
}

output "api_gateway_invoke_url" {
  description = "URL de invocação da API Gateway"
  value       = aws_api_gateway_stage.dev.invoke_url
}