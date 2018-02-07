# Big Data Module Work

## Current Solution

### Twitter Data
Currently, the streaming solution prototytpe involves the use of the Twitter API to stream realtime twitter data. This is accomplished via the `twitter4j` and 'spark-streaming-twitter' libraries. To run this solution there is a requirement for access to the [Twitter API](https://apps.twitter.com/) (current solution uses account attached to tjcurry@qinetiq.com). To change the API keys used edit the options in PROJECT_ROOT/twitter-stream-final/src/main/resources/twitter.txt as appropriate.

### Elasticsearch/Kibana/Spark
To add some convenience to eventually porting the solution, the elasticsearch, spark and kibana instances are managed through a `docker-compose` setup. As the current solution in this project has not been finalised, it has not been `sbt assemble`'d for execution through the spark client container (it uses local spark contexts as its run through the IDE). In the TwitterAPIStream.scala file, there is an elasticsearch config specifically set up to talk to the instance in the container which can be changed at a later date to talk to the spark instance inside the docker network.

### Running the current solution
Currently, the stream will acquire `hashtags`, `geo-data` and a text representation of the `place` from all incoming rdd's that have all three of these fields available. Obviously this might not be optimal data to collect but it essentially means we can plot locations of tweets to a map in kibana.

1. Run the containers - in the directory with the `docker-compose.yml` file (probably at PROJECT_ROOT), run `docker-compose up` (assuming docker/docker-compose is installed!)
   (NOTE: These cannot be run the same time as the CDH VM is open, as the same ports would be getting mapped!)
2. Load up [Kibana](http://localhost:5601)
3. A template must be created in elasticsearch for the incoming data, so we can map the location (latitude, longitude) to a geo_point object or elasticsearch wont be able to plot it to a map! - A working template can be found in PROJECT_ROOT/twitter-stream-final/src/main/resources/templatefores (ideally, this should be changed so the template is inserted at the startup of the es container). Copy this template into the management tools console in Kibana and run it .
3. Load up the solution in `IntelliJ`/`Eclipse` etc, and run `com.qq.spark.TwitterStreamAPI.scala` - This will log out the dataframes streamed in the console as:

	| hashtags | geo | place |    -  (these will appear empty in colsole until a tweet that has geo data enabled is found)

   and will also save the dataframes to the running elastisearch instance in the container.
3. Because we already told elasticsearch that the geo object is a geo_point we can plot it to a map, via the visualisations within kibana (and add it to a dashboard etc.)

### Other work done
Within the CDH VM (in VirtualBox), some work was done to parse raw twitter json through Kafka. I would use a Kafa console producer to push one (or more JSON) files into spark through the `kafka -> spark` connector. The lines would then be parsed through some json library (I used `play` json) and would output a dataframe similar to that in the above solution using the API. This was never fully completed as its not super easy to parse all the correct data but an example of how to parse the hashtags etc out of the json can be seen inside of the twitter stream project in the VM (accessible through IntelliJ).

### Futher work required
Ideally, I would have liked to got to a solution where both a assembled example of streaming the twitter data both with raw json and with the api could be used from within the docker container network, meaning it would be simple enough to use it on multiple machines or for demo's etc. If other graduates are to do the module, I think some good points to continue the work would be...

- Modify Twitter API solution to use Kafka to remove single point of failure
- Complete solution in a manner to this one but parsing raw json files
- Get elasticsearch templating for dataframes built into `docker-compose` container
- Use [Spark Notebooks](http://spark-notebook.io/) to potentially present some of this in a nicer way (also has map widgets that can plot geo data)
