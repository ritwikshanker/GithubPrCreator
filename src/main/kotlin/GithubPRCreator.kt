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
        return mapper.readValue(File("config.yml"), GitHubConfig::class.java)
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

    fun createPullRequest(repoName: String) {
        val repo = github.getRepository("${config.username}/$repoName")

        val mainBranch = repo.getBranch("master")
        val branchName = "feature/add-hello-file"


        repo.createRef("refs/heads/$branchName", mainBranch.shA1)

        val content = "Hello world"

        repo.createContent()
            .branch(branchName)
            .path("Hello.txt")
            .content(content)
            .message("Add Hello.txt file")
            .commit()

        repo.createPullRequest(
            "Add Hello.txt file",
            branchName,
            "master",
            "Adding Hello.txt file with 'Hello world' content"
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

        prCreator.createPullRequest(selectedRepo)
    } catch (e: Exception) {
        println("An error occurred: ${e.message}")
    }
}