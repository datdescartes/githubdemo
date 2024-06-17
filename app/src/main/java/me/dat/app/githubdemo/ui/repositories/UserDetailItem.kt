package me.dat.app.githubdemo.ui.repositories

import android.provider.CalendarContract.Colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import me.dat.app.githubdemo.entities.RepositoryEntity
import me.dat.app.githubdemo.entities.UserDetailEntity
import me.dat.app.githubdemo.ui.theme.GithubDemoTheme

@Composable
fun UserDetailItem(
    user: UserDetailEntity,
    avatarSize: Dp,
    textSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start, modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(user.avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Followers: ${user.followers} \nFollowing: ${user.following}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Text(
            text = user.fullName ?: "",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = textSize),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    GithubDemoTheme {
        UserDetailItem(
            user = UserDetailEntity(
                avatarUrl = "https://github.com/images/error/octocat_happy.gif",
                username = "octocat",
                fullName = "Monalisa Octocat",
                followers = 100,
                following = 10
            ),
            avatarSize = 64.dp,
            textSize = 16.sp,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
                .fillMaxWidth(),
        )
    }
}
