import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubConfig(
    @JsonProperty("personalAccessToken")
    val personalAccessToken: String = "",

    @JsonProperty("username")
    val username: String = ""
)