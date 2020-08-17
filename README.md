**Version**  
1.0.2

**Describe the bug**  
While validating phone numbers by regexp, wrong values do not cause error message.

**Expected behavior**  
There is a regular expression ^([1-9][0-9]{4,20})|(0[1-9][0-9]{2,18})$

Examples of valid values:
* 960737921588751247
* 07481910114962986
* 0728387718132

It is expected that values like the following will be rejected:
* ABC080033090300
* +080033090300
* 080033090300_alakazam

Although, only the last wrong value causes error message
"... '080033090300_alakazam' does not respect pattern '^([1-9][0-9]{4,20})|(0[1-9][0-9]{2,18})$'."

**To Reproduce**  
Steps to reproduce the behavior:
1. Run reproducer with 
    ```
    gradlew quarkusDev
2. Go to
    ```
   http://localhost:8080/resource/reproducer/swagger_ui/#/troubleTicket/createTroubleTicket
3. Send tickets with different phoneNumber values to see the result

**Reproducer**  
Please fill a reproducer for debugging purpose.
