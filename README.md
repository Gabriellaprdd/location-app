# Location App — Location Management & Review System

## Overview

Location App is an Android application developed to help users save, organize, and review places they have visited or want to remember.

The app allows users to create locations, add reviews, track visited places, and view simple insights from their saved data. This project was built to explore Android development concepts such as MVVM architecture, local database storage, and managing data between different parts of an application.

---

# System Architecture

The application follows an MVVM architecture to keep the code organized and easier to maintain.

## 1. UI Layer — Android Fragments

The UI handles user interaction and displays information from the application.

Main responsibilities:

- Display location lists
- Show location details
- Add and edit locations
- Display reviews
- Show review analysis

RecyclerView is used for displaying dynamic lists of locations and reviews.

---

## 2. Logic Layer — ViewModel

The ViewModel connects the UI with the database.

It handles:

- Managing application data
- Processing user actions
- Updating UI data when changes happen
- Keeping data available during screen changes

This keeps fragments focused on displaying information instead of handling database logic directly.

---

## 3. Data Layer — Room Database

The app uses Room Database to store data locally.

The database contains two main entities:

### Location

Stores information such as:

- Name
- Category
- Address
- Description
- Opening hours
- Visited status

### Review

Stores:

- Reviewer name
- Rating
- Comment
- Date
- Related location ID

Each location can have multiple reviews connected through the location ID.

---

# Design Decisions

## Why use MVVM Architecture?

At the beginning, it was possible to put most of the logic directly inside fragments. However, as more features were added, it became harder to manage.

Using MVVM provides:

- Better separation of responsibilities
- Easier debugging
- Cleaner code organization
- Better support for future features

The UI focuses only on displaying information, while ViewModel handles application logic.

---

## Why use Room Database?

Room was selected instead of manually managing SQLite because it provides:

 Easier database management
- Safer database queries
- Better integration with Android architecture components
- Support for observable data updates


Since the application mainly manages personal location data, local storage was enough for the current scope.

---

# Main Features

## Location Management

Users can save and manage places they are interested in.

Features:

- Create locations
- Edit information
- Delete locations
- Search saved places
- View detailed information

---

## Visited Tracking

Users can mark locations as visited.

The app uses this information to separate visited places from saved places and provide a better way to organize locations.

---

## Review System

Users can add personal reviews to locations.

The system supports:

- Ratings
- Comments
- Viewing previous reviews
- Linking reviews to specific locations

---

## Review Analysis

The application provides simple insights from stored reviews.

Examples:

- Average rating
- Most reviewed location
- Highest-rated location
- Lowest-rated location

---

# Development Challenges

## Managing Database Relationships

One challenge was connecting locations and reviews properly.

Since one location can have multiple reviews, the database needed a clear relationship between both tables.

Using location IDs made it easier to retrieve and manage related reviews.

---

## Keeping Data Updated Across Screens

Making sure every screen showed the latest information was another challenge.

For example, when a user adds a new review or edits a location, the changes need to appear immediately.

Using ViewModel and LiveData helped keep the UI synchronized with database changes.

---

## Handling Empty Data

The application also needed to handle situations where users had no data yet.

Examples:

- No saved locations
- No reviews
- No ratings available

Adding these states helped prevent unexpected behavior and improved the user experience.

---

# Testing and Results

## Location Features

Tested:

- Adding locations
- Editing details
- Deleting locations
- Searching locations

Result:

Location data was successfully stored and displayed from the local database.

---

## Review Features

Tested:

- Adding reviews
- Displaying reviews
- Updating ratings
- Handling locations without reviews

Result:

Reviews were correctly connected to their related locations.

---

## Data Persistence

Tested:

- Closing and reopening the application
- Updating saved data
- Loading existing records

Result:

User data remained available through Room Database.

---

# Technical Skills Gained

Through this project, I gained experience in:

- Android application development
- Kotlin programming
- MVVM architecture
- Room Database
- RecyclerView implementation
- Fragment navigation
- Local data management
- Database relationship design
- UI state management

---

# Lessons Learned

This project taught me that building an application is not only about creating features, but also about designing how different parts of the system communicate.

One of the biggest lessons was understanding the importance of separating UI, logic, and data handling. Having a clear structure made it easier to fix problems and add new features.

I also learned that planning the database structure early can save a lot of time later, especially when adding relationships between different types of data.

---

# Future Improvements

Possible improvements:

- Add Google Maps integration
- Add cloud synchronization
- Implement user accounts
- Improve location search and filtering
- Add automated testing
- Add location recommendations

---

# Technologies Used

## Development

- Kotlin
- Android Studio
- Android SDK

## Architecture & Storage

- MVVM Architecture
- ViewModel
- LiveData
- Room Database

## UI

- Android Fragments
- RecyclerView
- Navigation Component

## Libraries

- Glide (Image Loading)
- MPAndroidChart (Data Visualization)