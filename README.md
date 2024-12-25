# GitHub PR Creator

This script automates the creation of a pull request on GitHub.

**Features:**

* Connects to GitHub using a personal access token.
* Lists available repositories for the authenticated user.
* Allows the user to select a repository.
* Creates a new branch with a specified name.
* Creates a new file with the specified content.
* Creates a pull request to merge the new branch into the `master` branch.

**Prerequisites:**

* Java Development Kit (JDK) installed.
* Maven or Gradle (for dependency management).
* A GitHub account.
* A personal access token with the necessary scopes (e.g., `repo`).

**Usage:**

1. **Create a `config.yml` file** with the following structure:

```yaml
username: <your_github_username>
personalAccessToken: <your_github_personal_access_token>
