# EGA.Data.API.v3.KEY

This is a standalone Encryption Key server. This is a service to abstract encryption key management: this service returns a key for a file ID. The purpose of this server is to abstract customised key management solutions, so that RES_MVC can make a standardized REST call to obtain the encryotion key for an archived file, while allowing local installations to choose their own key management strategies. It is available via EUREKA using the service name `"KEY"`.

Dependency: 
* EGA Infrastructure https://github.com/EGA-archive/ega-api-infrastructure

Provides: 
* RES: requests encryption and decryption keys from this service

This service provides a very basic abstraction to handle encryption keys. Each installation will have to assess the security needs for this service. It should run in a private area shielded from outside access. Only RES should access this service.

This service uses a separate configuration XML to describe the keys used.

This service can be used (developed into) either as a proxy to existing key management systems, or into its own key management solution.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine.

### Installing

The repository contains pre-compiled jar files with the client. To build it on your local machine, run

```
mvn package
```

This will produce the KEY server jar file in the /target directory.

### Experimental [under construction]

The service can be deployed directly to a Docker container, using these instructions:

`wget https://raw.github.com/elixir-europe/ega-data-api-v3-key/master/docker/runfromsource.sh`  
`wget https://raw.github.com/elixir-europe/ega-data-api-v3-key/master/docker/build.sh`  
`chmod +x runfromsource.sh`  
`chmod +x build.sh`  
`./runfromsource.sh`  

These commands perform a series of actions:  
	1. Pull a build environment from Docker Hub  
	2. Run the 'build.sh' script inside of a transient build environment container.  
	3. The source code is pulled from GitHub and built  
	4. A Deploy Docker Image is built and the compiled service is added to the image  
	5. The deploy image is started; the service is automatically started inside the container  

The Docker image can also be obtained directly from Docker Hub:  

`sudo docker run -d -p 9094:9094 alexandersenf/ega_access`  or by running the `./runfromimage.sh` file.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
