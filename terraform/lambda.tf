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
  function_name    = "${var.project_name}-lambda"
  handler          = var.lambda_handler_name
  runtime          = var.lambda_runtime
  role             = aws_iam_role.lambda_exec_role.arn
  s3_bucket        = var.lambda_s3_bucket
  s3_key           = var.lambda_s3_key

  environment {
    variables = {
      REGION = var.aws_region
      COGNITO_SECRET_NAME = var.cognito_secret_name
    }
  }

  tags = {
    Project = var.project_name
  }
}

resource "aws_iam_policy" "lambda_secrets_policy" {
  name        = "${var.project_name}-lambda-secrets-policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue"
        ],
        Resource = "arn:aws:secretsmanager:us-east-1:499243079593:secret:tech-challenge/cognito/config-56bcIh*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_attach_secrets" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = aws_iam_policy.lambda_secrets_policy.arn
}

resource "aws_iam_policy" "lambda_cognito_policy" {
  name = "${var.project_name}-lambda-cognito-policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "cognito-idp:AdminGetUser",
          "cognito-idp:AdminCreateUser",
          "cognito-idp:AdminUpdateUserAttributes"
        ],
        Resource = "arn:aws:cognito-idp:us-east-1:499243079593:userpool/us-east-1_yo92WsG3Y"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_attach_cognito" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = aws_iam_policy.lambda_cognito_policy.arn
}
