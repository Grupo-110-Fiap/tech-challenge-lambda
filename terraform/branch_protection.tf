resource "github_branch_protection" "main_branch_protection" {
  repository_id = "tech-challenge-lambda"
  pattern       = "main"

  enforce_admins = true

  required_status_checks {
    strict   = true
    contexts = []
  }

  required_pull_request_reviews {
    dismiss_stale_reviews      = true
    required_approving_review_count = 1
  }

  restrict_pushes {
    blocks_creations = false
    push_allowances  = []
  }
}