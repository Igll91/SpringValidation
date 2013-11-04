package silvio.vuk.ag04.validation.spring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import silvio.vuk.java.ag04.validator.ValidationControlCenter;

@Controller
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	private final String UPLOAD_DIRECTORY_UNIX = "/var/tmp/";
	private final String UPLOAD_DIRECTORY = "C:\\Users\\Silvio\\Documents\\";
	private static String FS;
	
	static{
		if (ValidationControlCenter.isWindows()) {
			FS = "\\";
		} else if (ValidationControlCenter.isMac()) {
			FS = "/";
		} else if (ValidationControlCenter.isUnix()) {
			FS = "/";
		} else if (ValidationControlCenter.isSolaris()) {
			FS = "/";
		} else {
			// OS not supported
		}
	}
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = {"/index.html", "/"}, method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		return "Index";
	}

	@RequestMapping(value = "/Validator.html", method = RequestMethod.GET)
	public String validationRedirect(Locale locale, Model model) {
		logger.info("Redirecting home! The client locale is {}.", locale);

		return "Index";
	}

	@RequestMapping(value = "/Validator.html", method = RequestMethod.POST)
	public String validation(Locale locale, Model model, @ModelAttribute("uploadedFile") UploadedFile uploadedFile) {
		logger.info("Validation in process! The client locale is {}.", locale);

		InputStream inputStream = null;  
		OutputStream outputStream = null;  

		MultipartFile file = uploadedFile.getFile();    
		String fileName = file.getOriginalFilename();

		model.addAttribute("fileName", fileName);

		try 
		{  
			inputStream = file.getInputStream();  

			File newFile = new File(UPLOAD_DIRECTORY + fileName);  
			if (!newFile.exists())  
				newFile.createNewFile();  

			outputStream = new FileOutputStream(newFile);  
			int read = 0;  
			byte[] bytes = new byte[1024];  

			while ((read = inputStream.read(bytes)) != -1) {  
				outputStream.write(bytes, 0, read);  
			}  
			
			outputStream.close();
			String path = newFile.getAbsolutePath();

			ValidationControlCenter vcc = new ValidationControlCenter(path);

			vcc.validateFile();
			
			String filePathForReading = path.substring(0, path.lastIndexOf(FS));
        	filePathForReading +="/ValidationResults.txt";
        	
        	List<String> listOfStrings = readValidationFile(filePathForReading);
        	
        	model.addAttribute("results", listOfStrings);
			
			model.addAttribute("message", "File validated Successfully!");
		} 
		catch(FileNotFoundException ex)
		{
			model.addAttribute("message", "File validation Failed due to " + ex);
			logger.error(ex.getMessage());
		}
		catch(InvalidPropertiesFormatException ex)
		{
			model.addAttribute("message", "File validation Failed due to " + ex);
			logger.error(ex.getMessage());
		}
		catch(IOException ex)
		{
			model.addAttribute("message", "File validation Failed due to " + ex);
			logger.error(ex.getMessage());
		}  

		return "Validator";
	}

	private List<String> readValidationFile(String path) throws FileNotFoundException, IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(path));

		List<String> listOfStrings = new ArrayList<String>();

		String line;
		while ((line = br.readLine()) != null) 
		{
			listOfStrings.add(line);
		}

		br.close();

		return listOfStrings;
	}
}
