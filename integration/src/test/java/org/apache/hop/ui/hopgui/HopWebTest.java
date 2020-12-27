/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.ui.hopgui;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HopWebTest {
  private WebDriver driver;
  private Actions actions;
  private String baseUrl;
  private WebElement element;
  private WebDriverWait wait;

  @Before
  public void setUp() throws Exception {
    boolean isHeadless = Boolean.parseBoolean( System.getProperty( "headless.unittest", "true" ) );
    ChromeOptions options = new ChromeOptions();
    if ( isHeadless ) {
      options.addArguments( "headless" );
    }
    options.addArguments( "--window-size=1280,800" );
    driver = new ChromeDriver( options );
    actions = new Actions( driver );
    wait = new WebDriverWait( driver, 5 );
    baseUrl = System.getProperty( "test.baseurl", "http://localhost:8080" );
    driver.get( baseUrl );
    driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );
  }

  @Test
  public void testAppLoading() {
    assertEquals( driver.getTitle(), "Hop" );
  }

  /*
  @Test
  public void testContextDialog() {
    clickElement( "//div[text() = 'File']" );
    clickElement( "//div[text() = 'New']" );
    wait.until( ExpectedConditions.presenceOfElementLocated( By.xpath( "//div[text() = 'Select the item to create']" ) ) );
  }
  */

  private void clickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    element.click();
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
