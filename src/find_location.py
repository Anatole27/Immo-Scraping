'''
Created on Dec 1, 2018

@author: anatole

'''

import re
from selenium import webdriver
import time

class Coords:
    lat = 0;
    lon = 0;
    
    
    
class LocFinder:
    def __init__(self):
        options = webdriver.FirefoxOptions();
        options.headless = True;
        self.driver = webdriver.Firefox(options=options);
    
    def lookForSource(self, url):
        self.driver.get(url)
        self.page_source = self.driver.page_source;
    
    def getLocation(self):
        coords = Coords();
        match = re.search('(?<="lat\"\:)[0-9]*\.[0-9]*', self.page_source);
        if match:
            coords.lat = match.group(0);
        match = re.search('(?<="lng\"\:)[0-9]*\.[0-9]*', self.page_source);
        if match:
            coords.lon = match.group(0);
        return coords;
    
    def getGes(self):
        coords = Coords();
        match = re.search('(?<="GES","value_label":")[A-z]*(?=[ ])', self.page_source);
        if match:
            return match.group(0);
        else:
            return "Non"
    
    def getEnergy(self):
        coords = Coords();
        match = re.search('(?<="Classe énergie","value_label":")[A-z]*(?=[ ])', self.page_source);
        if match:
            return match.group(0);
        else:
            return "Non"
    
    def closeBrowser(self):
        self.driver.quit()