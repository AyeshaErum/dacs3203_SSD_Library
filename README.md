📚 Library Management System

A fully interactive Library Management System featuring a graphical user interface (GUI), secure authentication, and MySQL integration. The application allows users to register, borrow books, manage profiles, and enables admins to oversee the entire library system.

👥 Team Members

Ayesha Erum — 60304595

Saima Nasin — 60304985

✨ Features

🔹 User Features

Create an account

Log in using username and password

Browse available books

Borrow and return books

View profile and borrowing history

Edit basic user details

🔹 Admin Features

Add new books

Edit or remove book entries

View all registered users

Track borrowed/returned books

Manage system data

🔹 GUI Features

User-friendly and clean interface

Intuitive navigation

Easy book management

Error and success notifications

Separate interfaces for user and admin roles

🛠 Technologies Used

Java (GUI-based application)

MySQL (database backend)

JDBC (database connectivity)

⚙️ Setup Instructions

1️⃣ Clone the Repository
git clone https://github.com/yourname/library-management-system.git


2️⃣ Open the Project

Open the folder in your preferred IDE:
VS Code, IntelliJ IDEA, Eclipse, or NetBeans.

3️⃣ Configure the Database

Create a MySQL database (example: library_db)

Import the provided SQL file (if included)

Update database credentials inside your database connection class:

String url = "jdbc:mysql://localhost:3306/library_db";
String username = "root";
String password = "your_password";

4️⃣ Run the Application

Run the main class:

App.java

