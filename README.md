# Symptom-Checker-APP
This app supports 2 devices: Mobile and Watch, and uses EndlessMedical API to implement symptoms analysis.

## Mobile terminal
There are 8 fragments in mobile, then we use the navigation.xml to implement the transition between fragments

Fragment-1: UserLoginProfileFragment
- Users can sign in/up their accounts by providing their profile, such as user-image, username, and password.
- Firebase is used to check if the current user is a new user
- Appropriate Toast messages are shown when users click Sign-In & Sign-Up buttons
- For new users: 
   1. They should provide all profile, then click Sign Up -> Sign In to navigate to to Fragment-2
   1. A new Firebase fork is created to store their profile
- For users who registrated before:
   1. Only username and password are required, then click Sign In -> to Fragment-2.
   1. All Firebase data corresponding to this use are downloaded, such as the symptoms they updated before 
- At the same time, the user-image and username are sent to watch through DataClient

Fragment-2: UserMainMenuFragment
- This fragment show all the functions our app has
   1. Basic information are shown, such as the Number of Selected Symptoms
   1. Check box to enable Heart Rate detection by sending command to watch 
      * HR is used as a symptom automatically ***
      * when HR exceeds thresholds (i.e., min = 50, max = 120), our app is started immediately if it is in background

   1. Check box to confirm if "Use default values for symptoms not selected when analyzing disease"
   1. Button to add symptoms -> to Fragment-3
   1. Button to get probable disease by analyzing the symptoms the users have provided -> to Fragment-6
   1. Button to log out current account -> to Fragment-1

Fragment-3: SearchSymptomsFragment
- This frament includes 3 parts
- Part-1: a complete list including all symptom querys provided by EndlessMedical API
   1. It is clickable, user can click any symptoms they want
   1. We provide a search-match bar, so user can search any symptoms they want by entering key words
   1. For symptoms whose values are integer & double (i.e., Age = 22, BMI = 20.0), SymptomsSeekBar fragment is navigated when they are clicked
   1. For symptoms whose values are choice string (i.e., What is the timing of dyspnea progression = No dyspnea at all ), 
        SymptomsSpinner fragment is navigated when they are clicked

- Part-2: a complete list including all symptoms confirmed by users
   1. For each list item, the symptom query and the corresponding symptom value (i.e., BMI = 20) are shown
   1. Provides an edit button that allows the user to edit symptom values at any time
   1. Provides an delete button that allows the user to delete the selected symptom directly
- Part-3: Provides a Save button to navigate to Fragment-1

Fragment-4: SymptomsSeekBarFragment
- User can adjust the symptom value by dragging seekbar
- The symptom value is shown in real-time (i.e., follows seekbar)
- Provides a Confirm button, store the selected symptom value and -> Fragment-3
- Provides a Cancel button, just back to -> Fragment-3

- At the same time, the UI showing symptom with seekbar appears in watch automatically 
- Similar UI as Mobile shown in watch (i.e., seekbar, confirm button, etc.)

- Users can answer either on the mobile or the watch 
- When users answer on mobile (i.e., click confirm button), the watch UI can navigate back to main activity automatically
- When users answer on watch (i.e., click confirm button), the mobile UI can navigate back to Fragment-3 automatically 

- The selected symptoms and corresponding values are stored on both devices (i.e., synchronously)

Fragment-5: SymptomsSpinnerFragment
- This fragment provides similar functions as Fragment-4, but gives different UI (i.e., spinner choice)
- Synchronized watch is also enabled

After providing all symptoms (i.e., these symptoms are also updated to EndlessMedical server through corresponding API), 
users can analyze possible diseases by clicking the analyze button in Fragment-3

Fragment-6:  AnalyzeFragment
- A complete list of possible diseases analyzed by the API is shown
- The diseases are ranked by likelihood
- Provides button to navigate back to UserMainMenuFragment
- Provides button to access a detailed medical summary for users -> Fragment-7

Fragment-7:  PatientDocumentationFragment
- Provides a detailed medical summary (i.e., chief complaint, recommended specialist, etc)
- Provides button to navigate back to AnalyzeFragment
- Provides button to access recommend medical specialists in Lausanne -> Fragment-8


Fragment-8:  RecommendedSpecialistFragment
- This fragment is divided into two parts
- Part-1 shows the google map which locates current location of users automatically
- Part 2 shows the full list of recommended hospitals and their locations are marked on the map
    users can quickly access them from the map








