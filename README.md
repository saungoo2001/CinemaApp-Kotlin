# CinemaApp-Kotlin

A fully functional Android application built with **Kotlin** that allows users to browse cinemas, movies, showtimes, and book seats. The app integrates **Firebase Authentication** and **Realtime Database** for user management and profile handling, and uses **ImgBB API** for profile image uploads.

---

## Features

- **User Authentication**  
  Sign up and log in using email/password with Firebase Authentication.

- **Profile Management**  
  - Edit display name  
  - Upload profile image (camera or gallery)  
  - Display profile image in app and navigation drawer  

- **Cinema & Movie Listings**  
  - Browse multiple cinemas  
  - View movies with genres, poster, and overview  
  - Select showtimes for each movie  

- **Seat Booking**  
  - See available and booked seats  
  - Book seats for a selected showtime  

- **Real-time Data Updates**  
  - Firebase Realtime Database stores cinemas, movies, showtimes, and user profiles  
  - Profile image updates instantly in app  

- **ImgBB Integration**  
  - Upload profile images to ImgBB  
  - Automatic retrieval and display in app  

---

## Screenshots

<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/f83ac634-9c7d-4fa2-b5d8-e67296653881" />
<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/61bbde9f-af75-4631-abd6-c59ce742e808" />

<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/c49d9a57-3996-41fa-9ded-e69a60c2ce8b" />

<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/a571893c-ca42-4228-a528-1e60b840e0c1" />
<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/66828763-76e1-47be-b56d-46ccfb0c9493" />
<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/24628a85-71e0-43cb-8f4e-7c725c684e9a" />


---

## Technologies Used

- Kotlin  
- Android Studio  
- Firebase Authentication & Realtime Database  
- Glide (for image loading)  
- Retrofit (for API calls)  
- ImgBB API  

---

## Project Structure

- `MainActivity.kt` – Main activity with cinema & movie listing  
- `SeatSelectionActivity.kt` – Seat booking activity  
- `auth/` – Login, SignUp, and ForgetPassword modules  
- `adapter/` – RecyclerView adapters for cinemas, movies, and showtimes  
- `models/` – Data models for Cinema, Movie, Showtime, User, etc.  
- `utils/` – Helper functions (e.g., Base64 conversion, image upload)  

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/saungoo2001/CinemaApp-Kotlin.git
