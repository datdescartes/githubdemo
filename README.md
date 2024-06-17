# GitHub Demo App

This is a simple GitHub demo app built with Jetpack Compose, Retrofit, and Dagger Hilt. The app allows users to search for GitHub users, view user details, and list repositories of a selected user. The app also supports pagination and displays repository details in a card layout.

## Features

- Search GitHub users
- View user details (avatar, username, full name, followers, following)
- List user repositories with details (name, description, language, stars)
- Open repository in an external browser
- Pagination for user repositories
- Responsive UI with animations

## Getting Started

### Prerequisites

- Android Studio Koala or later
- A GitHub Personal Access Token (PAT) for API authentication

### Installation

1. **Clone the repository**
2. **Add your GitHub Personal Access Token**
Create or open the local.properties file in the root directory of your project and add your GitHub Personal Access Token:
```
   authToken=your_personal_access_token
```
