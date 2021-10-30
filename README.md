# Calorie Counter backend

Features of Calorie counter project: 

•    API Users are able to create an account and log in.
•    All API calls are authenticated.
•    Implemented three roles with different permission levels: a regular user would only be able to CRUD on their owned records, a user manager would be able to CRUD only users, and an admin would be able to CRUD all records and users.
•    Each entry has a date, time, text, and number of calories.
•    If the number of calories is not provided, the API connects to a Calories API provider (for example https://www.nutritionix.com) and gets the number of calories for the entered meal.
•    User setting – Expected number of calories per day.
•    Each entry has an extra boolean field set to true if the total for that day is less than expected number of calories per day, otherwise false.
•    The API is able to return data in the JSON format.
•    The API provides filter capabilities for all endpoints that return a list of elements, as well supports pagination.
•    The API filtering allows using parenthesis for defining operations precedence and use any combination of the available fields. The supported operations include or, and, eq (equals), ne (not equals), gt (greater than), lt (lower than).
Example -> (date eq '2016-05-01') AND ((number_of_calories gt 20) OR (number_of_calories lt 10)).
•    Unit and e2e tests.
