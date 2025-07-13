# Private Server Extension Framework

## Table of Contents
1. [Introduction](#introduction)
2. [Server Extensions](#server-extensions) 
3. [World Subscriptions](#world-subscriptions)
   1. [Requirements](#requirements)
   2. [User Experience](#user-experience)
   3. [Data validation](#data-validation)
4. [Binary Distributions](#binary-distributions)
   1. [Requirements](#requirements-1)
   2. [Configuration files](#configuration-files)
   3. [Application Updates](#application-updates)
   4. [Branding](#branding)
5. [Appendix](#appendix)
   1. [Startup Flags](#startup-flags)  
   2. [World Subscription JSON Example](#world-subscription-json-example)  

## Introduction

Without official Runescape Classic servers to connect to, RSC+ primarily exists as a tool for software preservation.
For this reason, any and all modifications to the underlying 234 mudclient _must_ account for authenticity with regard
to the original server behaviors.

That said, private servers for Runescape Classic have existed for a very long time and will likely continue to persist,
as many people are quite fond of this somewhat-strange and often-unforgiving game. As such, RSC+ may be used to connect
to any RSC private server that is capable of speaking the authentic 234 protocol.

Historically, many private servers have chosen to implement customizations that never existed in the actual game.
A common example of this is a global chat feature, allowing sparse populations of active players to communicate with
each other in-game, without the authentic need for location proximity. Other customizations may exist simply due to a
lack of official data.

To accommodate such features, RSC+ offers the concept of _Server Extensions_ as a means of both satisfying its
philosophy of software preservation while also providing the capability to support various tools to enhance a user's
experience when playing on a private server.

## Server Extensions

Server extensions exist as a means by which custom private server features may be implemented within the RSC+ code base.

Developers wishing to add support for their private server of choice may begin doing so by first registering an
identifier for the server within the `ServerExtensions` class — see its [documentation](../ServerExtensions.java)
for technical implementation guidance. This class provides a framework of methods which can be used throughout the RSC+
codebase to conditionally modify various behaviors at runtime, in response to the presence of an active extension.

Server extensions are automatically enabled upon selection of an RSC+ world which contains one of the registered
server extension identifier values in its `server_extension` field. The "about" tab in the RSC+ settings window will
always indicate whether an extension is currently active, though an extension developer may choose to implement other
means by which to present this information to a player.

Extensions are categorized into one of two types: exclusive and common. Exclusive extensions are intended for sole use
by particular private servers to provide unique client features to their players. Private server hosts wishing to
leverage this type of extension MUST register and maintain a [World Subscription](#world-subscriptions), as exclusivity
is enforced by means of host domain comparison between the subscription defined in the RSC+ source code and the server
URL defined within a world file. 

Conversely, common extensions may be enabled for use by any non-exclusive world. This type of extension would typically
be used by private servers that share a common underlying codebase, alleviating the need for every individual host to
register their own unique extension and World Subscription within the RSC+ codebase, while allowing them to leverage
new features for these extensions as they are developed. Common extensions MAY NOT be used in conjunction with either
World Subscriptions or [Binary Distributions](#binary-distributions), as they exist to serve the needs of a particular
private server and are maintained by their own operators.

For organizational consistency, classes related to a particular extension MUST reside within the [Client.Extensions](.)
package, and MUST be named in an easily-distinguishable manner.

When recording an RSC+ replay, the client will detect and capture the presence of an active extension, which will then
be re-enabled when the file is played back at a later time.

*Note that feature enhancements which facilitate cheating in the form of macros or automation will not be accepted into
the RSC+ source code.*

## World Subscriptions

RSC+ uses world files to store connection data for playable servers. A common pain point for private server players
and operators alike is the distribution and acquisition of these world files. This is not a concept common to most
modern games, and users lacking in technical aptitude can easily get confused by the notion of having to manually
administer configuration files on their device.

To alleviate this problem, RSC+ builds on the idea of _Server Extensions_ to offer a _World Subscription_ system,
responsible for the automatic management of world files pertaining to a particular private server. This feature is
activated through the use of a [startup flag](#world-subscription-feature) and when enabled, the client will
automatically download world files for the specified subscription during application startup.

When opting-in to this system, server hosts are responsible for providing APIs which serve JSON data corresponding in
content to the world files used for connecting to their game servers. API operators may modify the contents of this
data at any point in time and may add, update, or remove world definitions as-needed. Clients subscribed to the API will
automatically detect any changes to the data during startup and modify the user's world files on disk to reflect them.

World Subscriptions are inherently tied to the concept of a Server Extension, and it is not possible to leverage
this feature without minimally registering one. Conversely, use of a World Subscription is not required to enable the
use of server extension features.

Usage of the World Subscription system requires the definition of a `world_id` value for all worlds defined within
the subscription API data. This field is used to identify each distinct world and is necessary even when offering a
single world to play on. Distinct worlds are grouped together as-needed by their `WorldType`, defined during initial
world subscription setup within the RSC+ codebase. These types are used during the world download process and can be
leveraged to offer more granular server extension features. An example use case would be visual delineation of "veteran"
worlds on the world selection screen. Note that significantly-impactful gameplay features should not be gated behind
these types, as there are edge-cases in which types are not strictly enforced between worlds of a specific subscription.

Although explicit usage of the aforementioned startup flag is required to enable the automatic world file downloading
behaviors, RSC+ will nevertheless detect when a world file corresponds to a defined world subscription and will reach
out to the registered subscription API to verify its contents. When connection details for a given world file match
those of a particular world defined in the JSON data, values for certain fields will be overridden or added to the
existing world file, including the server extension and world ID fields. In doing so, server extension features will
get automatically enabled for existing users who had already been able to connect to the private server in the past,
without the need to modify world files, download additional software, or specify Java startup flags.

### Requirements

Registration of a World Subscription within the RSC+ codebase is done within the
[`ServerExtensions`](../ServerExtensions.java)  class, which provides documentation describing the technical process.

Before all else, a server extension corresponding to the World Subscription must be registered within the RSC+
codebase, and its identifier will be used as the argument to the [startup flag](#world-subscription-feature) which
enables the world subscription feature.

As mentioned above, world data must be served from a JSON endpoint hosted by the private server operators. This endpoint
represents a collection of worlds to be downloaded and its URL will need to be provided during the registration process.
Alongside the URL, extension developers are expected to define a `WorldType` for each endpoint, which directly
corresponds to the `world_id` defined for each world. See the documentation within the [`WorldType`](./WorldType.java)
class for more information.

Multiple endpoints may be registered for a given world subscription, which can then be used to logically group together
sets of individual worlds, as in the example mentioned above regarding veteran-type worlds. Data for these worlds
would be defined in a secondary endpoint, allowing users to selectively disable the downloading of worlds for these
types, and allowing extension developers to offer unique features pertaining to the group, through evaluation of the
`world_id` value for the user's currently-selected world.

JSON data corresponding to world files is expected to follow a specific format. See the provided
[example](#world-subscription-json-example) within the appendix for details.

Subscription operators are ultimately responsible for the validity of the data hosted within each of their JSON
endpoints. While a number of measures have been taken within the RSC+ codebase to resiliently handle certain
misconfigurations, care must be taken to ensure that all data accurately defines the world files that will ultimately
be downloaded to the user's device. Certain misconfigurations, such as the duplication of world definitions, may lead to
situations where RSC+ may mishandle world files. Others, such as an incorrectly-specified `server_extension`, will
outright prevent the user from attempting to submit their credentials to the server endpoint. Furthermore, it is
recommended that all JSON endpoints are hosted at the same domain, as partial download failures may block client
connections under certain conditions or otherwise break certain non-critical extension-related functionalities.

Note that functionality exists to handle situations wherein a server's host domain _MUST_ change in response to an
emergency such as a domain hijacking. See the documentation for the `addFormerHostUrl` method within the
`WorldSubscription` class for more information.

### User experience

When the World Subscription feature is enabled, RSC+ will reach out to the configured world subscription endpoints
during application startup to fetch the world data. Connection timeouts have been established to prevent excessive wait
times in the case that the JSON endpoints are slow to respond or cannot be reached. Because this process occurs before
RSC+ has had a chance to render any window frames, a modal will be displayed to the user after some time has passed,
informing them that data fetching is underway.

Once the application has finished loading, users may navigate to the `World List` tab within the settings window to view
information about the downloaded worlds. When multiple `WorldTypes` have been defined, the user will be presented with
an option to selectively enable or disable the future downloading of worlds belonging to a particular type. Users will
be prevented from editing details about worlds obtained from a subscription and manual alterations to the underlying ini
files will be discarded upon future world downloads in subsequent launches. Note that this is not the case for worlds
that have simply been upgraded due to detection of a world subscription, when the feature has not been enabled via the
startup flag. 

Worlds obtained from a subscription will always be displayed first in the world selection list at the bottom of the
login window. Because world names are displayed when a user hovers over a world selection box, care should be taken to
appropriately name each world in a way that will be easily understood by the user.

### Data validation

Certain logic has been added to RSC+ to prevent undesirable outcomes from the accidental or nefarious modification
of data within the world subscription endpoints.

After data has been successfully obtained from the subscription endpoints, each downloaded world will be processed
through a series of data integrity validations. Certain outcomes may occur in the event that a violation is detected,
the most severe of which would preclude the user's ability to connect to the game world.

These actions primarily exist as a safety mechanism to protect the user. As an example, in the event that an attacker is
able to successfully compromise a private server's subscription endpoints, they may choose to modify defined world
server URLs such that connections will proxy through the attacker's own servers, which could then be used to collect
user credentials. RSC+ protects users from this scenario by ensuring that the host domain of the server URL within the
world file matches the one registered by the extension developer within the codebase. When a mismatch of this type is
detected, the client will block connections to the game server when the user attempts to authenticate. Another example
of a safety mechanism built into the validation process is the prevention of subscription endpoints from declaring usage
of another private server's extension features.

For development purposes, these validations do not apply to worlds whose server URLs point to a local address.

## Binary Distributions

Many end-users today struggle with the technical prerequisites necessary for launching Java applications on their
devices. To alleviate these struggles, RSC+ provides explicit support for extension developers who wish to package it
within a distributable system-specific binary, alongside a bundled Java Runtime executable. Doing so simplifies and
streamlines the installation process for players wishing to use RSC+ to connect to their private server and allows
for the standardization of key Java components such as the specific JRE version and heap usage configurations.

RSC+ itself [provides](https://github.com/RSCPlus/rscplus/releases/tag/Latest) official binaries for download and
private server operators may simply choose to advise the subset of their players wishing to utilize RSC+ to install it.
This binary does not target any particular server and does not provide any world files out-of-the-box. As such, private
server operators that choose this option will need to continue to distribute RSC+ world files containing connection data
to their servers. Because [server extensions](#server-extensions) are activated upon selection of a compatible world
file, choosing to go down this route will not preclude a private server's players from utilizing their extension
features.

That said, specific options are available to extension developers who wish to package their own binary distributions.
Doing so will enable operators to customize those Java features described above, as well as some parts of the overall
RSC+ UX for a more personalized experience for their players. Furthermore, this feature may be used in conjunction with
a [World Subscription](#world-subscriptions), as Java packaging technologies typically offer the ability to specify
desired startup flags when launching the embedded Java process. Opting in to both of these features allows private
server operators to provide a very seamless installation experience for these players, at the cost of additional
maintenance and support, described in more detail below.

_Developers wishing to leverage the Binary Distribution feature are expected to manage their own build systems and means
of distribution._

### Requirements

Much like the World Subscription feature described earlier in this document, use of the Binary Distribution feature
first requires registration of a Server Extension, as its identifier is used as the argument to the primary
[startup flag](#binary-launching) required when launching RSC+ through a binary. 

Certain other information is needed in order for RSC+ to properly function when launched in this way and this data must
be provided at the point of registration within the [`ServerExtensions`](../ServerExtensions.java) class. Technical
guidance for accomplishing this is provided within its class documentation.

As alluded to above, RSC+ flexes certain behaviors when launched through a binary. These include the location of the
directories used to store various [configuration files](#configuration-files), facilitation of binary and client
[updates](#application-updates), and certain user experience [customizations](#branding). Additional information
regarding all of these processes and their individual registration requirements are described in detail below.

### Configuration files

By default, the official RSC+ binary creates certain directories on the user's device to store configuration and user
files necessary for its execution. These directories are system-dependent and follow standard software development
practices, e.g. within the `%APPDATA%` location on a Windows operating system. Within those directories, RSC+ creates
an `RSCPlus` directory for its own use.

To support its co-existence with customized binary distributions, RSC+ requires extension developers to choose a prefix,
up to 4 characters in length, during the registration process. This prefix is then used when creating its own
configuration directories. For example, developers of the "Kittens" Server Extension may choose a prefix of "Meow",
leading to the creation of the "MeowRSCPlus" directory.

### Application updates

To simplify the means by which a user would update their installed binary, RSC+ executes a set of procedures that work
in conjunction with its regular client updating process. This process checks for application updates during startup,
prompts users to upgrade when new versions are available, and performs as much of the update as possible. Integration
with this process is a mandatory requirement for opting-in to the Binary Distribution feature.

At the time of this writing, the RSC+ codebase only supports a very specific set of binary update procedures that work
in conjunction with its own [build scripts](../../../binaries) and additional development would be required to support
other updating methodologies.

Adherence to these procedures requires several prerequisites:
- Each system-specific binary MUST be built in the same manner as utilized by RSC+ itself
- All system-specific binary downloads MUST be hosted at the same relative location
- Each binary download MUST reside alongside a corresponding version file which MUST contain ONLY the binary version
- Binary versioning MUST adhere to the following format: `yyyyMMdd.HHmmss`
- The current binary version must be passed as an argument to one of the required [startup flags](#binary-launching)

During the registration process, extension developers are required to provide a URL corresponding to the base location
where the binaries and their respective version files may be found, along with file names for each binary download and
its corresponding version file. Lastly, each binary download must be tagged with one of the available `BINARY_TYPE`
enumeration values.

When the binary is launched, the aforementioned update process begins by fetching the contents of the version file that
corresponds to the system it's being launched from. If the returned version is greater than the current version
specified in the launch flag, the user will be prompted to accept the update, which will be performed in a different
manner depending on the user's operating system. When no application update is available, on all operating systems
except for Linux, RSC+ will then check its own code repository for an available client update and will prompt the user
to accept it, should one be available. Because the Linux binary distribution MUST be in the form of an AppImage, and
AppImages are read-only by design, RSC+ will not be able to replace the underlying RSC+ JAR file on the user's system.

### Branding

RSC+ provides the ability to customize some aspects of its user experience when launched through a binary.

The first of these customizations is mandatory and will happen as a result of providing the required application prefix
during registration, as described in the [configuration files](#configuration-files) section. Doing so will result in
the addition of the specified prefix to every "RSCPlus" label displayed in various places throughout the client.

The second and final customization allows extension developers to provide a custom icon to be used within the various
Swing frames created by the client. These icons MUST be placed within a folder in the
[assets/extensions](../../../assets/extensions) directory and will need to be specified within the `main` method inside
the [Launcher](../Launcher.java) class.

## Appendix

### Startup flags

#### World subscription feature

`-DdownloadWorlds=[Extension ID]`

#### Binary launching

`-DusingBinary=[Exension ID]`

`-DbinaryVersion=[Application Version]`

### World Subscription JSON Example

```json
[
  {
    "name": "Kittens World 1",
    "url": "world1.kittens.domain",
    "port": "55555",
    "rsa_pub_key": "31415926535897932384626433832795028841971693993751058",
    "rsa_exponent": "12345",
    "servertype": "1",
    "hiscores_url": "https://kittens.domain/user/%USERNAME%",
    "registration_api_url": "https://kittens.domain/registeration",
    "server_extension": "Kittens",
    "world_id": "kittens1"
  },
  {
    "name": "Kittens World 2",
    "url": "world2.kittens.domain",
    "port": "55556",
    "rsa_pub_key": "31415926535897932384626433832795028841971693993751058",
    "rsa_exponent": "12345",
    "servertype": "1",
    "hiscores_url": "https://kittens.domain/user/%USERNAME%",
    "registration_api_url": "https://kittens.domain/registeration",
    "server_extension": "Kittens",
    "world_id": "kittens2"
  }
]
```
