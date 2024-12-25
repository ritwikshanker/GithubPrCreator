import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubConfig(
    @JsonProperty("personalAccessToken")
    val personalAccessToken: String = "",  // Default value added

    @JsonProperty("username")
    val username: String = ""  // Default value added
)