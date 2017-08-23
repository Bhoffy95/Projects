import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumTesting {

	
	public static void main(String[] args){
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\bhoffman\\Downloads\\chromedriver_win32\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		//navigate to tirefinder
		driver.get("http://google.com");
		
		
		driver.findElement(By.id("auto_store_ID")).sendKeys("");
		driver.findElement(By.id("auto_payroll_ID")).sendKeys("");
		
		driver.findElement(By.id("auto_enter")).click();
		
		WebDriverWait wait = new WebDriverWait(driver, 5);
		WebElement yearDrop = wait.until(ExpectedConditions.elementToBeClickable(By.id("auto-year")));
		driver.findElement(By.id("auto-year")).click();
		Select years = new Select(driver.findElement(By.id("auto-year")));
		WebDriverWait yearWait = new WebDriverWait(driver, 5);
		Boolean yearElement = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("auto-year"), "2013"));
		years.selectByVisibleText("2013");
		
		WebDriverWait makeDropWait = new WebDriverWait(driver, 5);
		WebElement makeDrop = wait.until(ExpectedConditions.elementToBeClickable(By.id("auto-make")));
		driver.findElement(By.id("auto-make")).click();
		Select make = new Select(driver.findElement(By.id("auto-make")));
		WebDriverWait makeWait = new WebDriverWait(driver, 5);
		Boolean makeElement = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("auto-make"), "Audi"));
		make.selectByVisibleText("Audi");
		
		driver.findElement(By.id("auto-model")).click();
		Select model = new Select(driver.findElement(By.id("auto-model")));
		WebDriverWait modelWait = new WebDriverWait(driver, 5);
		Boolean modelElement = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("auto-model"), "A5"));
		model.selectByVisibleText("A5");
		
		driver.findElement(By.id("auto-trim")).click();
		Select trims = new Select(driver.findElement(By.id("auto-trim")));
		WebDriverWait trimWait = new WebDriverWait(driver, 5);
		Boolean trimElement = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("auto-trim"), "Convertible"));
		trims.selectByVisibleText("Convertible");
		
		driver.findElement(By.id("auto-assembly")).click();
		Select assembly = new Select(driver.findElement(By.id("auto-assembly")));
		WebDriverWait assemblyWait = new WebDriverWait(driver, 5);
		Boolean assemblyElement = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("auto-assembly"), "245/40-18"));
		assembly.selectByVisibleText("245/40-18");
		
		WebDriverWait recommendWait = new WebDriverWait(driver, 5);
		WebElement recommendElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("auto-view-recommendations")));
		driver.findElement(By.id("auto-view-recommendations")).click();
		
		WebElement detailsElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("auto-product-list-view-details")));
		List<WebElement> a=driver.findElements(By.id("auto-product-list-view-details"));
		a.get(3).click();
		
		driver.quit();

	}
}
