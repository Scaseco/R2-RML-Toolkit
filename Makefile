CWD = $(shell pwd)

# Source: https://stackoverflow.com/questions/4219255/how-do-you-get-the-list-of-targets-in-a-makefile
.PHONY: help

.ONESHELL:
help:   ## Show these help instructions
	@sed -rn 's/^([a-zA-Z_-]+):.*?## (.*)$$/"\1" "\2"/p' < $(MAKEFILE_LIST) | xargs printf "make %-20s# %s\n"

deb-re: ## Reinstall deb (requires prior build)
	@p1=`find rml-pkg-parent/rml-pkg-deb-cli/target | grep '\.deb$$'`
	sudo dpkg -i "$$p1"

