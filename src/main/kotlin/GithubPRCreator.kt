import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.File
import java.util.Scanner
import java.util.InputMismatchException

/**
 * This class creates a pull request on a GitHub repository.
 */
class GitHubPRCreator {
    private lateinit var github: GitHub
    private val config: GitHubConfig = loadConfig()

    /**
     * Loads the configuration from the "config.yml" file.
     *
     * @throws RuntimeException if the file cannot be loaded or is not formatted correctly.
     * @return the loaded GitHubConfig object.
     */
    private fun loadConfig(): GitHubConfig {
        val mapper = ObjectMapper(YAMLFactory())
        return try {
            mapper.readValue(File("config.yml"), GitHubConfig::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load config.yml. Please ensure it exists and is correctly formatted.", e)
        }
    }

    /**
     * Connects to GitHub using the personal access token from the configuration.
     */
    fun connect() {
        github = GitHubBuilder()
            .withOAuthToken(config.personalAccessToken)
            .build()
    }

    /**
     * Lists available repositories for the user and prompts the user to select one.
     *
     * @return the name of the selected repository.
     */
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

    /**
     * Creates a pull request on the specified repository.
     *
     * @param repoName the name of the repository.
     * @param branchName the name of the branch to create for the pull request.
     * @param filePath the path to the file to add to the pull request.
     * @param fileContent the content of the file to add.
     */
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
