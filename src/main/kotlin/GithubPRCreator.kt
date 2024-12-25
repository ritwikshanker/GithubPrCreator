import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.File
import java.util.Scanner
import java.util.InputMismatchException

class GitHubPRCreator {
    private lateinit var github: GitHub
    private val config: GitHubConfig = loadConfig()

    private fun loadConfig(): GitHubConfig {
        val mapper = ObjectMapper(YAMLFactory())
        return try {
            mapper.readValue(File("config.yml"), GitHubConfig::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load config.yml. Please ensure it exists and is correctly formatted.", e)
        }
    }

    fun connect() {
        github = GitHubBuilder()
            .withOAuthToken(config.personalAccessToken)
            .build()
    }

    fun listAndSelectRepository(): String {
        val repos = github.getUser(config.username).repositories.values.toList()

        println("\nAvailable repositories:")
        repos.forEachIndexed { index, repo ->
            println("${index + 1}. ${repo.name}")
        }

        var selection: Int
        val scanner = Scanner(System.`in`)

        while (true) {
            print("\nSelect a repository (1-${repos.size}): ")
            try {
                selection = scanner.nextInt()
                if (selection in 1..repos.size) {
                    break
                }
                println("Invalid selection. Please try again.")
            } catch (e: InputMismatchException) {
                println("Please enter a valid number.")
                scanner.next()
            }
        }
        return repos[selection - 1].name
    }

    fun createPullRequest(repoName: String, branchName: String, filePath: String, fileContent: String) {
        val repo = github.getRepository("${config.username}/$repoName")
        val masterBranch = repo.getBranch("master")

        repo.createRef("refs/heads/$branchName", masterBranch.shA1)
        repo.createContent()
            .branch(branchName)
            .path(filePath)
            .content(fileContent)
            .message("Add $filePath")
            .commit()

        repo.createPullRequest(
            "Add $filePath",
            branchName,
            "master",
            "Adding $filePath with content"
        )

        println("\nPull request created successfully!")
    }
}

fun main() {
    val prCreator = GitHubPRCreator()
    try {
        println("Connecting to GitHub")
        prCreator.connect()

        val selectedRepo = prCreator.listAndSelectRepository()
        println("\nCreating pull request for repository: $selectedRepo")

        prCreator.createPullRequest(
            repoName = selectedRepo,
            branchName = "feature/add-hello-file",
            filePath = "Hello.txt",
            fileContent = "Hello world"
        )
    } catch (e: Exception) {
        println("An error occurred: ${e.message}")
    }
}
