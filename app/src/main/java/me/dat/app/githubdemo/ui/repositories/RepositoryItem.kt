package me.dat.app.githubdemo.ui.repositories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.dat.app.githubdemo.entities.RepositoryEntity

@Composable
fun RepositoryItem(
    repo: RepositoryEntity,
    onClick: (RepositoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(repo) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    text = repo.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.End,
                ) {
                    repo.language?.let { language ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(getLanguageColor(language), shape = CircleShape)
                            )
                            Text(
                                text = language,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = repo.stargazersCount.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = repo.description ?: "",
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun getLanguageColor(language: String): Color {
    return when (language) {
        "Kotlin", "Java", "Scala", "Groovy" -> Color(0xFFA97BFF)
        "Swift", "Objective-C" -> Color(0xFFB07219)
        "JavaScript", "TypeScript" -> Color(0xFFF1E05A)
        "Ruby", "Python", "PHP" -> Color(0xFF3572A5)
        "C#", "C++", "C", "Rust" -> Color(0xFFF34B7D)
        else -> Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
fun RepositoryItemPreview() {
    RepositoryItem(
        repo = RepositoryEntity(
            id = 1,
            name = "Hello-WorldHello-WorldHello-WorldHello-WorldHello-World",
            fullName = "octocat/Hello-World",
            ownerAvatarUrl = "https://github.com/images/error/octocat_happy.gif",
            ownerLogin = "octocat",
            htmlUrl = "https://github.com/octocat/Hello-World",
            description = "This is your first repository",
            language = "Kotlin",
            stargazersCount = 42
        ),
        onClick = {}
    )
}
