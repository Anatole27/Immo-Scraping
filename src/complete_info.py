'''
Created on Dec 2, 2018

@author: anatole
'''

import csv 
import find_location as src
import distance


input_file = '../res/leboncoin_2018_12_02.csv';
output_file = '../res/leboncoin_2018_12_02_pp.csv'

# Get site
site_str = "leboncoin";

print("Launching firefox")
finder = src.LocFinder();
                
with open(input_file, encoding = "ISO-8859-1") as csv_input_file:
    with open(output_file,"w+", encoding = "ISO-8859-1") as csv_output_file:
        csv_reader = csv.reader(csv_input_file, delimiter=',')
        line_count = 0
        
        spamwriter = csv.writer(csv_output_file, delimiter=',', quoting=csv.QUOTE_MINIMAL)
        spamwriter.writerow(['website','url','type','surface','pieces','prix','quartier','description','coords','trajet boulot (min)','GES','classe energie'])
        
        for row in csv_reader:
            if line_count == 0:
#                 print(f'Column names are {", ".join(row)}')
                line_count += 1
            else:
                line_count += 1
                
                
                # Copy url
                url_str = row[3];
                print(url_str);
                
                # Get type
                type_str = row[8];
                
                # Get surface
                sfc_str = row[5];
                
                # Get pieces
                pieces_str = row[6];
                
                # Get prix
                prix_str = row[4];
                
                # Get quartier
                quartier_str = row[12];
                
                # Get desc
                desc_str = row[11];
                
                # Look for source
                coords = finder.lookForSource(row[3])
                
                # Find location
                coords = finder.getLocation()
                if coords.lat != 0:
                    coords_str = f'{coords.lat} {coords.lon}'
                else:
                    coords_str = "?";
                
                # Get duree trajet
                if coords.lat != 0:
                    duree = distance.getDistanceBoulot(coords.lat, coords.lon)
                    duree_str = str(duree)
                else:
                    duree_str = "?";
                print(duree_str)
                
                # Get GES
                ges_str = finder.getEnergy()
                
                # Get energy
                energy_str = finder.getGes()
            
                # Write
                spamwriter.writerow([site_str, url_str, type_str, sfc_str, 
                                     pieces_str, prix_str, quartier_str, 
                                     desc_str, coords_str, duree_str, ges_str, energy_str]);

            
#                 if line_count > 5:
#                     break
        print(f'Processed {line_count} lines.')
        
finder.closeBrowser()