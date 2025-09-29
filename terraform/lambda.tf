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