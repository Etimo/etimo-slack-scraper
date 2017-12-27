# Etimo slack blog
Simple software that will bundle conversations from any slack channel
into daily markdown files usable together with [JekyllRB](https://jekyllrb.com/)
## Use
Create your config from the dummy in src/main/resources. 
Run the application with a -c flag pointing to your config.

## Config
```json 
{
	"slackToken":"NotARealSlackTockenAtAll,It'sFake",
	"slackChannel":"etimo_external",
	"postDirectory":"/home/erik/code/etimo-strap/_posts",
	"assetDirectory":"/home/erik/code/etimo-strap/assets",
	"startDate":"2017-11-20",
	"baseTitle":"Teknik-chatt",
	"blogPeriodBreak":"daily",
	"mergePeriod":"2",
	"timeZoneId":"default"
	
}
```
- slackToken: An api key for the user that will be used to scrape the channel.
- slackChannel: The name of the channel to scrape
- postDirectory: The posts directory of your jekyllrb site. Or any other directory to save the files to.
- assetDirectory: Images and thumbnails are downloaded here 
- startDate: Date in YYYY-MM-dd format, messages sent in the channel on this day or later will be included in posts.
- baseTitle: This is the prefix to all automated post titles.
- timeZoneId: Id of a timeZone for JodaTime. All post times will be shown using this timezone.
	- See [JodaTime Zone Ids](http://joda-time.sourceforge.net/timezones.html)). 
	- Set to "default" to use the timeZone of the system.

- blogPeriodBreak: Determines in what periods posts will be bundled. 
	- daily - messages from 24h periods starting from startDate will be bundled.	
	- monday,tuesday,wednesday,thursday,friday,saturday or sunday - Messages in between two of the set weekday will be bundled into a post. startDate  until the first such day will be a post.
	- 72 (or any other number) set a custom bundling period in hours starting from startDate.
- mergePeriod - Messages that are sent by the same person with no intermediate messages by another person will be merged in the blog post, if they are within this timespan.
