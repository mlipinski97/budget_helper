# budget helper 
budget helper is front-end application used in my project for engineering degree

It is android app allowing easier managing of home budget.
User is able to:
- plan expenses for chosen dates and categorize them
- group expenses within budget lists (each list has its own set by user currency and time period)
- share budget lists with other users of application through simple friend system. Every user with access to shared budget list can edit and add new content.
- make graph summaries of expenses split into categories for chosen month 

Each expense tracks who modified it last thus assgning who created it or executed it (checked "done" mark on that expense)

Used Java Android for minmum SDK version 24+ with Gradle building tool.
Server requests handled with retrofit.
Server runs on Heroku and uses DBMS PosgreSQL


