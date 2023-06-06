# CC_LeitTHau
Clean Code Project from Bodo Thausing and Claudia Leitinger

This prokject is a Webcrawler which crawls multiple website concurrently and also translates the headers of the sides and saves it in a markdown.md file, which you find in the project unter recources. 

To build and run, edit the run configurations and add the depth, the target language, you want the headers translated into, and the website you want to crawl. 
Seperate them by space. (e.g. 3 DE https://www.bodofoto.at/  https://www.google.at )

Translator API used: 
Google Translator via rapidapi.com

Package manager used:
Maven

Dependencies:
Jackson-Databind
Jacoco
Mockito core
Junit
