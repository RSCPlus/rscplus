# Twitch Chat in rscplus

In order to use Twitch chat in rscplus, you are required to register an app on Twitch and use it in order to get your OAuth token.

1. Go to [Twitch's developer console](https://dev.twitch.tv/console/apps).
2. Press "Register Your Application".
3. Enter a (unique) name for the application, and set the OAuth redirect URL to `http://localhost:1337`. Also set the Category to `Chat Bot`.
4. Press Manage on your application, and copy your Client ID.
5. Once registered, go to this page in your browser and authorise, replacing `<CLIENTID>` with your actual client ID:
```https://id.twitch.tv/oauth2/authorize?client_id=<CLIENTID>&redirect_uri=http://localhost:1337&response_type=token&scope=chat%3Aread+chat%3Aedit```
6. Once authorised, retrieve your token from the `access_token` part of the web address.
7. In rscplus Streaming & Privacy settings, enter the channel you want to talk in, your Twitch username (the same one used to authorised the application), and your OAuth token retrieved above.
8. When you log in, you should be able to talk in Twitch chat by prepending your message with `/`