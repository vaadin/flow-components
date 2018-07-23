# Cookie Consent for Vaadin Flow

### Overview
Vaadin Cookie Consent is a web component used for showing a cookie consent banner the first time a user visits the application.

### License & Author

This add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3).

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add Cookie Consent to your project:

```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-cookie-consent-flow</artifactId>
    <version>1.0.0.beta1</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
CookieConsent cookieConsent = new CookieConsent();
add(cookieConsent);
```