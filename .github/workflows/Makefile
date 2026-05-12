teardown:
	@echo "DANGER: this deletes the Terraform state backend for $(PROJECT)."
	@echo "  bucket     = $(BUCKET)"
	@echo "  lock_table = $(LOCK_TABLE)"
	@read -p "Type the project name to confirm: " confirm && \
		[ "$$confirm" = "$(PROJECT)" ] || (echo "aborted"; exit 1)
	-aws s3 rm s3://$(BUCKET) --recursive
	-aws s3api delete-bucket --bucket $(BUCKET) --region $(REGION)
	-aws dynamodb delete-table --table-name $(LOCK_TABLE) --region $(REGION)

# ── GitHub-AWS OIDC bootstrap ────────────────────────────────────────────
# Example:
#   make github-access GH_REPO=comp-proj-assignment/scg-asgn-infra ROLE_NAME=comp-proj-infra-deployer
github-access:
	@[ -n "$(GH_REPO)" ]   || (echo "GH_REPO=<owner>/<repo> required" >&2; exit 1)
	@[ -n "$(ROLE_NAME)" ] || (echo "ROLE_NAME=<role> required" >&2; exit 1)
	GH_REPO="$(GH_REPO)" \
	ROLE_NAME="$(ROLE_NAME)" \
	POLICY_ARN="$(POLICY_ARN)" \
	  bash tools/aws-github-access.sh

github-access-teardown:
	@[ -n "$(ROLE_NAME)" ] || (echo "ROLE_NAME=<role> required" >&2; exit 1)
	ROLE_NAME="$(ROLE_NAME)" bash tools/aws-github-access-teardown.sh
