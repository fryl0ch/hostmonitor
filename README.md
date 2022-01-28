# hostmonitor

v1.0.0

This was a project I did back in 2004 that I recently re-discovered from an old hard drive.

Boss wanted notifications when a site or service went offline, and logs of ping times when responding normally. I think he used it for a week or two to make a decision between hosting providers, and it hasn't seen the light of day since. He suggested I open source it at the time, but I forgot. Whoops.

Highlights:

- SMTP client to send email notifications
- Base64 encoder/decoder to attach logs to emails
- monitor HTTP/HTTPS sites, or TCP/UDP services
- output ping logs to CSV

see hostmonitor.conf for configuration options
