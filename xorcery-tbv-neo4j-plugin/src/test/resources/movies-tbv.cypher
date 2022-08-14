CREATE (TheMatrix:Movie:INSTANCE {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheMatrix_R:Movie_R:RESOURCE {id: 'TheMatrix'})
CREATE (Keanu:Person:INSTANCE {name:'Keanu Reeves', born:1964})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Keanu_R:Person_R:RESOURCE {id: 'Keanu'})
CREATE (Carrie:Person:INSTANCE {name:'Carrie-Anne Moss', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Carrie_R:Person_R:RESOURCE {id: 'Carrie'})
CREATE (Laurence:Person:INSTANCE {name:'Laurence Fishburne', born:1961})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Laurence_R:Person_R:RESOURCE {id: 'Laurence'})
CREATE (Hugo:Person:INSTANCE {name:'Hugo Weaving', born:1960})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Hugo_R:Person_R:RESOURCE {id: 'Hugo'})
CREATE (AndyW:Person:INSTANCE {name:'Andy Wachowski', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AndyW_R:Person_R:RESOURCE {id: 'AndyW'})
CREATE (LanaW:Person:INSTANCE {name:'Lana Wachowski', born:1965})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(LanaW_R:Person_R:RESOURCE {id: 'LanaW'})
CREATE (JoelS:Person:INSTANCE {name:'Joel Silver', born:1952})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JoelS_R:Person_R:RESOURCE {id: 'JoelS'})
CREATE
  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrix_R),
  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrix_R),
  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrix_R),
  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrix_R),
  (AndyW)-[:DIRECTED]->(TheMatrix_R),
  (LanaW)-[:DIRECTED]->(TheMatrix_R),
  (JoelS)-[:PRODUCED]->(TheMatrix_R)

CREATE (Emil:Person:INSTANCE {name:"Emil Eifrem", born:1978})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Emil_R:Person_R:RESOURCE {id: 'Emil'})
CREATE
   (Emil)-[:ACTED_IN {roles:["Emil"]}]->(TheMatrix_R)

CREATE (TheMatrixReloaded:Movie:INSTANCE {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheMatrixReloaded_R:Movie_R:RESOURCE {id: 'TheMatrixReloaded'})
CREATE
  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixReloaded_R),
  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixReloaded_R),
  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixReloaded_R),
  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixReloaded_R),
  (AndyW)-[:DIRECTED]->(TheMatrixReloaded_R),
  (LanaW)-[:DIRECTED]->(TheMatrixReloaded_R),
  (JoelS)-[:PRODUCED]->(TheMatrixReloaded_R)

CREATE (TheMatrixRevolutions:Movie:INSTANCE {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheMatrixRevolutions_R:Movie_R:RESOURCE {id: 'TheMatrixRevolutions'})
CREATE
  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixRevolutions_R),
  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixRevolutions_R),
  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixRevolutions_R),
  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixRevolutions_R),
  (AndyW)-[:DIRECTED]->(TheMatrixRevolutions_R),
  (LanaW)-[:DIRECTED]->(TheMatrixRevolutions_R),
  (JoelS)-[:PRODUCED]->(TheMatrixRevolutions_R)

CREATE (TheDevilsAdvocate:Movie:INSTANCE {title:"The Devil's Advocate", released:1997, tagline:'Evil has its winning ways'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheDevilsAdvocate_R:Movie_R:RESOURCE {id: 'TheDevilsAdvocate'})
CREATE (Charlize:Person:INSTANCE {name:'Charlize Theron', born:1975})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Charlize_R:Person_R:RESOURCE {id: 'Charlize'})
CREATE (Al:Person:INSTANCE {name:'Al Pacino', born:1940})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Al_R:Person_R:RESOURCE {id: 'Al'})
CREATE (Taylor:Person:INSTANCE {name:'Taylor Hackford', born:1944})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Taylor_R:Person_R:RESOURCE {id: 'Taylor'})
CREATE
  (Keanu)-[:ACTED_IN {roles:['Kevin Lomax']}]->(TheDevilsAdvocate_R),
  (Charlize)-[:ACTED_IN {roles:['Mary Ann Lomax']}]->(TheDevilsAdvocate_R),
  (Al)-[:ACTED_IN {roles:['John Milton']}]->(TheDevilsAdvocate_R),
  (Taylor)-[:DIRECTED]->(TheDevilsAdvocate_R)

CREATE (AFewGoodMen:Movie:INSTANCE {title:"A Few Good Men", released:1992, tagline:"In the heart of the nation's capital, in a courthouse of the U.S. government, one man will stop at nothing to keep his honor, and one will stop at nothing to find the truth."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AFewGoodMen_R:Movie_R:RESOURCE {id: 'AFewGoodMen'})
CREATE (TomC:Person:INSTANCE {name:'Tom Cruise', born:1962})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TomC_R:Person_R:RESOURCE {id: 'TomC'})
CREATE (JackN:Person:INSTANCE {name:'Jack Nicholson', born:1937})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JackN_R:Person_R:RESOURCE {id: 'JackN'})
CREATE (DemiM:Person:INSTANCE {name:'Demi Moore', born:1962})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(DemiM_R:Person_R:RESOURCE {id: 'DemiM'})
CREATE (KevinB:Person:INSTANCE {name:'Kevin Bacon', born:1958})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(KevinB_R:Person_R:RESOURCE {id: 'KevinB'})
CREATE (KieferS:Person:INSTANCE {name:'Kiefer Sutherland', born:1966})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(KieferS_R:Person_R:RESOURCE {id: 'KieferS'})
CREATE (NoahW:Person:INSTANCE {name:'Noah Wyle', born:1971})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(NoahW_R:Person_R:RESOURCE {id: 'NoahW'})
CREATE (CubaG:Person:INSTANCE {name:'Cuba Gooding Jr.', born:1968})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CubaG_R:Person_R:RESOURCE {id: 'CubaG'})
CREATE (KevinP:Person:INSTANCE {name:'Kevin Pollak', born:1957})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(KevinP_R:Person_R:RESOURCE {id: 'KevinP'})
CREATE (JTW:Person:INSTANCE {name:'J.T. Walsh', born:1943})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JTW_R:Person_R:RESOURCE {id: 'JTW'})
CREATE (JamesM:Person:INSTANCE {name:'James Marshall', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JamesM_R:Person_R:RESOURCE {id: 'JamesM'})
CREATE (ChristopherG:Person:INSTANCE {name:'Christopher Guest', born:1948})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ChristopherG_R:Person_R:RESOURCE {id: 'ChristopherG'})
CREATE (RobR:Person:INSTANCE {name:'Rob Reiner', born:1947})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RobR_R:Person_R:RESOURCE {id: 'RobR'})
CREATE (AaronS:Person:INSTANCE {name:'Aaron Sorkin', born:1961})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AaronS_R:Person_R:RESOURCE {id: 'AaronS'})
CREATE
  (TomC)-[:ACTED_IN {roles:['Lt. Daniel Kaffee']}]->(AFewGoodMen_R),
  (JackN)-[:ACTED_IN {roles:['Col. Nathan R. Jessup']}]->(AFewGoodMen_R),
  (DemiM)-[:ACTED_IN {roles:['Lt. Cdr. JoAnne Galloway']}]->(AFewGoodMen_R),
  (KevinB)-[:ACTED_IN {roles:['Capt. Jack Ross']}]->(AFewGoodMen_R),
  (KieferS)-[:ACTED_IN {roles:['Lt. Jonathan Kendrick']}]->(AFewGoodMen_R),
  (NoahW)-[:ACTED_IN {roles:['Cpl. Jeffrey Barnes']}]->(AFewGoodMen_R),
  (CubaG)-[:ACTED_IN {roles:['Cpl. Carl Hammaker']}]->(AFewGoodMen_R),
  (KevinP)-[:ACTED_IN {roles:['Lt. Sam Weinberg']}]->(AFewGoodMen_R),
  (JTW)-[:ACTED_IN {roles:['Lt. Col. Matthew Andrew Markinson']}]->(AFewGoodMen_R),
  (JamesM)-[:ACTED_IN {roles:['Pfc. Louden Downey']}]->(AFewGoodMen_R),
  (ChristopherG)-[:ACTED_IN {roles:['Dr. Stone']}]->(AFewGoodMen_R),
  (AaronS)-[:ACTED_IN {roles:['Man in Bar']}]->(AFewGoodMen_R),
  (RobR)-[:DIRECTED]->(AFewGoodMen_R),
  (AaronS)-[:WROTE]->(AFewGoodMen_R)

CREATE (TopGun:Movie:INSTANCE {title:"Top Gun", released:1986, tagline:'I feel the need, the need for speed.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TopGun_R:Movie_R:RESOURCE {id: 'TopGun'})
CREATE (KellyM:Person:INSTANCE {name:'Kelly McGillis', born:1957})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(KellyM_R:Person_R:RESOURCE {id: 'KellyM'})
CREATE (ValK:Person:INSTANCE {name:'Val Kilmer', born:1959})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ValK_R:Person_R:RESOURCE {id: 'ValK'})
CREATE (AnthonyE:Person:INSTANCE {name:'Anthony Edwards', born:1962})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AnthonyE_R:Person_R:RESOURCE {id: 'AnthonyE'})
CREATE (TomS:Person:INSTANCE {name:'Tom Skerritt', born:1933})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TomS_R:Person_R:RESOURCE {id: 'TomS'})
CREATE (MegR:Person:INSTANCE {name:'Meg Ryan', born:1961})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MegR_R:Person_R:RESOURCE {id: 'MegR'})
CREATE (TonyS:Person:INSTANCE {name:'Tony Scott', born:1944})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TonyS_R:Person_R:RESOURCE {id: 'TonyS'})
CREATE (JimC:Person:INSTANCE {name:'Jim Cash', born:1941})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JimC_R:Person_R:RESOURCE {id: 'JimC'})
CREATE
  (TomC)-[:ACTED_IN {roles:['Maverick']}]->(TopGun_R),
  (KellyM)-[:ACTED_IN {roles:['Charlie']}]->(TopGun_R),
  (ValK)-[:ACTED_IN {roles:['Iceman']}]->(TopGun_R),
  (AnthonyE)-[:ACTED_IN {roles:['Goose']}]->(TopGun_R),
  (TomS)-[:ACTED_IN {roles:['Viper']}]->(TopGun_R),
  (MegR)-[:ACTED_IN {roles:['Carole']}]->(TopGun_R),
  (TonyS)-[:DIRECTED]->(TopGun_R),
  (JimC)-[:WROTE]->(TopGun_R)

CREATE (JerryMaguire:Movie:INSTANCE {title:'Jerry Maguire', released:2000, tagline:'The rest of his life begins now.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JerryMaguire_R:Movie_R:RESOURCE {id: 'JerryMaguire'})
CREATE (ReneeZ:Person:INSTANCE {name:'Renee Zellweger', born:1969})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ReneeZ_R:Person_R:RESOURCE {id: 'ReneeZ'})
CREATE (KellyP:Person:INSTANCE {name:'Kelly Preston', born:1962})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(KellyP_R:Person_R:RESOURCE {id: 'KellyP'})
CREATE (JerryO:Person:INSTANCE {name:"Jerry O'Connell", born:1974})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JerryO_R:Person_R:RESOURCE {id: 'JerryO'})
CREATE (JayM:Person:INSTANCE {name:'Jay Mohr', born:1970})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JayM_R:Person_R:RESOURCE {id: 'JayM'})
CREATE (BonnieH:Person:INSTANCE {name:'Bonnie Hunt', born:1961})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BonnieH_R:Person_R:RESOURCE {id: 'BonnieH'})
CREATE (ReginaK:Person:INSTANCE {name:'Regina King', born:1971})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ReginaK_R:Person_R:RESOURCE {id: 'ReginaK'})
CREATE (JonathanL:Person:INSTANCE {name:'Jonathan Lipnicki', born:1996})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JonathanL_R:Person_R:RESOURCE {id: 'JonathanL'})
CREATE (CameronC:Person:INSTANCE {name:'Cameron Crowe', born:1957})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CameronC_R:Person_R:RESOURCE {id: 'CameronC'})
CREATE
  (TomC)-[:ACTED_IN {roles:['Jerry Maguire']}]->(JerryMaguire_R),
  (CubaG)-[:ACTED_IN {roles:['Rod Tidwell']}]->(JerryMaguire_R),
  (ReneeZ)-[:ACTED_IN {roles:['Dorothy Boyd']}]->(JerryMaguire_R),
  (KellyP)-[:ACTED_IN {roles:['Avery Bishop']}]->(JerryMaguire_R),
  (JerryO)-[:ACTED_IN {roles:['Frank Cushman']}]->(JerryMaguire_R),
  (JayM)-[:ACTED_IN {roles:['Bob Sugar']}]->(JerryMaguire_R),
  (BonnieH)-[:ACTED_IN {roles:['Laurel Boyd']}]->(JerryMaguire_R),
  (ReginaK)-[:ACTED_IN {roles:['Marcee Tidwell']}]->(JerryMaguire_R),
  (JonathanL)-[:ACTED_IN {roles:['Ray Boyd']}]->(JerryMaguire_R),
  (CameronC)-[:DIRECTED]->(JerryMaguire_R),
  (CameronC)-[:PRODUCED]->(JerryMaguire_R),
  (CameronC)-[:WROTE]->(JerryMaguire_R)

CREATE (StandByMe:Movie:INSTANCE {title:"Stand By Me", released:1986, tagline:"For some, it's the last real taste of innocence, and the first real taste of life. But for everyone, it's the time that memories are made of."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(StandByMe_R:Movie_R:RESOURCE {id: 'StandByMe'})
CREATE (RiverP:Person:INSTANCE {name:'River Phoenix', born:1970})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RiverP_R:Person_R:RESOURCE {id: 'RiverP'})
CREATE (CoreyF:Person:INSTANCE {name:'Corey Feldman', born:1971})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CoreyF_R:Person_R:RESOURCE {id: 'CoreyF'})
CREATE (WilW:Person:INSTANCE {name:'Wil Wheaton', born:1972})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(WilW_R:Person_R:RESOURCE {id: 'WilW'})
CREATE (JohnC:Person:INSTANCE {name:'John Cusack', born:1966})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JohnC_R:Person_R:RESOURCE {id: 'JohnC'})
CREATE (MarshallB:Person:INSTANCE {name:'Marshall Bell', born:1942})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MarshallB_R:Person_R:RESOURCE {id: 'MarshallB'})
CREATE
  (WilW)-[:ACTED_IN {roles:['Gordie Lachance']}]->(StandByMe_R),
  (RiverP)-[:ACTED_IN {roles:['Chris Chambers']}]->(StandByMe_R),
  (JerryO)-[:ACTED_IN {roles:['Vern Tessio']}]->(StandByMe_R),
  (CoreyF)-[:ACTED_IN {roles:['Teddy Duchamp']}]->(StandByMe_R),
  (JohnC)-[:ACTED_IN {roles:['Denny Lachance']}]->(StandByMe_R),
  (KieferS)-[:ACTED_IN {roles:['Ace Merrill']}]->(StandByMe_R),
  (MarshallB)-[:ACTED_IN {roles:['Mr. Lachance']}]->(StandByMe_R),
  (RobR)-[:DIRECTED]->(StandByMe_R)

CREATE (AsGoodAsItGets:Movie:INSTANCE {title:'As Good as It Gets', released:1997, tagline:'A comedy from the heart that goes for the throat.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AsGoodAsItGets_R:Movie_R:RESOURCE {id: 'AsGoodAsItGets'})
CREATE (HelenH:Person:INSTANCE {name:'Helen Hunt', born:1963})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(HelenH_R:Person_R:RESOURCE {id: 'HelenH'})
CREATE (GregK:Person:INSTANCE {name:'Greg Kinnear', born:1963})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(GregK_R:Person_R:RESOURCE {id: 'GregK'})
CREATE (JamesB:Person:INSTANCE {name:'James L. Brooks', born:1940})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JamesB_R:Person_R:RESOURCE {id: 'JamesB'})
CREATE
  (JackN)-[:ACTED_IN {roles:['Melvin Udall']}]->(AsGoodAsItGets_R),
  (HelenH)-[:ACTED_IN {roles:['Carol Connelly']}]->(AsGoodAsItGets_R),
  (GregK)-[:ACTED_IN {roles:['Simon Bishop']}]->(AsGoodAsItGets_R),
  (CubaG)-[:ACTED_IN {roles:['Frank Sachs']}]->(AsGoodAsItGets_R),
  (JamesB)-[:DIRECTED]->(AsGoodAsItGets_R)

CREATE (WhatDreamsMayCome:Movie:INSTANCE {title:'What Dreams May Come', released:1998, tagline:'After life there is more. The end is just the beginning.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(WhatDreamsMayCome_R:Movie_R:RESOURCE {id: 'WhatDreamsMayCome'})
CREATE (AnnabellaS:Person:INSTANCE {name:'Annabella Sciorra', born:1960})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AnnabellaS_R:Person_R:RESOURCE {id: 'AnnabellaS'})
CREATE (MaxS:Person:INSTANCE {name:'Max von Sydow', born:1929})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MaxS_R:Person_R:RESOURCE {id: 'MaxS'})
CREATE (WernerH:Person:INSTANCE {name:'Werner Herzog', born:1942})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(WernerH_R:Person_R:RESOURCE {id: 'WernerH'})
CREATE (Robin:Person:INSTANCE {name:'Robin Williams', born:1951})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Robin_R:Person_R:RESOURCE {id: 'Robin'})
CREATE (VincentW:Person:INSTANCE {name:'Vincent Ward', born:1956})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(VincentW_R:Person_R:RESOURCE {id: 'VincentW'})
CREATE
  (Robin)-[:ACTED_IN {roles:['Chris Nielsen']}]->(WhatDreamsMayCome_R),
  (CubaG)-[:ACTED_IN {roles:['Albert Lewis']}]->(WhatDreamsMayCome_R),
  (AnnabellaS)-[:ACTED_IN {roles:['Annie Collins-Nielsen']}]->(WhatDreamsMayCome_R),
  (MaxS)-[:ACTED_IN {roles:['The Tracker']}]->(WhatDreamsMayCome_R),
  (WernerH)-[:ACTED_IN {roles:['The Face']}]->(WhatDreamsMayCome_R),
  (VincentW)-[:DIRECTED]->(WhatDreamsMayCome_R)

CREATE (SnowFallingonCedars:Movie:INSTANCE {title:'Snow Falling on Cedars', released:1999, tagline:'First loves last. Forever.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SnowFallingonCedars_R:Movie_R:RESOURCE {id: 'SnowFallingonCedars'})
CREATE (EthanH:Person:INSTANCE {name:'Ethan Hawke', born:1970})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(EthanH_R:Person_R:RESOURCE {id: 'EthanH'})
CREATE (RickY:Person:INSTANCE {name:'Rick Yune', born:1971})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RickY_R:Person_R:RESOURCE {id: 'RickY'})
CREATE (JamesC:Person:INSTANCE {name:'James Cromwell', born:1940})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JamesC_R:Person_R:RESOURCE {id: 'JamesC'})
CREATE (ScottH:Person:INSTANCE {name:'Scott Hicks', born:1953})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ScottH_R:Person_R:RESOURCE {id: 'ScottH'})
CREATE
  (EthanH)-[:ACTED_IN {roles:['Ishmael Chambers']}]->(SnowFallingonCedars_R),
  (RickY)-[:ACTED_IN {roles:['Kazuo Miyamoto']}]->(SnowFallingonCedars_R),
  (MaxS)-[:ACTED_IN {roles:['Nels Gudmundsson']}]->(SnowFallingonCedars_R),
  (JamesC)-[:ACTED_IN {roles:['Judge Fielding']}]->(SnowFallingonCedars_R),
  (ScottH)-[:DIRECTED]->(SnowFallingonCedars_R)

CREATE (YouveGotMail:Movie:INSTANCE {title:"You've Got Mail", released:1998, tagline:'At odds in life... in love on-line.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(YouveGotMail_R:Movie_R:RESOURCE {id: 'YouveGotMail'})
CREATE (ParkerP:Person:INSTANCE {name:'Parker Posey', born:1968})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ParkerP_R:Person_R:RESOURCE {id: 'ParkerP'})
CREATE (DaveC:Person:INSTANCE {name:'Dave Chappelle', born:1973})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(DaveC_R:Person_R:RESOURCE {id: 'DaveC'})
CREATE (SteveZ:Person:INSTANCE {name:'Steve Zahn', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SteveZ_R:Person_R:RESOURCE {id: 'SteveZ'})
CREATE (TomH:Person:INSTANCE {name:'Tom Hanks', born:1956})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TomH_R:Person_R:RESOURCE {id: 'TomH'})
CREATE (NoraE:Person:INSTANCE {name:'Nora Ephron', born:1941})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(NoraE_R:Person_R:RESOURCE {id: 'NoraE'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Joe Fox']}]->(YouveGotMail_R),
  (MegR)-[:ACTED_IN {roles:['Kathleen Kelly']}]->(YouveGotMail_R),
  (GregK)-[:ACTED_IN {roles:['Frank Navasky']}]->(YouveGotMail_R),
  (ParkerP)-[:ACTED_IN {roles:['Patricia Eden']}]->(YouveGotMail_R),
  (DaveC)-[:ACTED_IN {roles:['Kevin Jackson']}]->(YouveGotMail_R),
  (SteveZ)-[:ACTED_IN {roles:['George Pappas']}]->(YouveGotMail_R),
  (NoraE)-[:DIRECTED]->(YouveGotMail_R)

CREATE (SleeplessInSeattle:Movie:INSTANCE {title:'Sleepless in Seattle', released:1993, tagline:'What if someone you never met, someone you never saw, someone you never knew was the only someone for you?'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SleeplessInSeattle_R:Movie_R:RESOURCE {id: 'SleeplessInSeattle'})
CREATE (RitaW:Person:INSTANCE {name:'Rita Wilson', born:1956})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RitaW_R:Person_R:RESOURCE {id: 'RitaW'})
CREATE (BillPull:Person:INSTANCE {name:'Bill Pullman', born:1953})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BillPull_R:Person_R:RESOURCE {id: 'BillPull'})
CREATE (VictorG:Person:INSTANCE {name:'Victor Garber', born:1949})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(VictorG_R:Person_R:RESOURCE {id: 'VictorG'})
CREATE (RosieO:Person:INSTANCE {name:"Rosie O'Donnell", born:1962})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RosieO_R:Person_R:RESOURCE {id: 'RosieO'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Sam Baldwin']}]->(SleeplessInSeattle_R),
  (MegR)-[:ACTED_IN {roles:['Annie Reed']}]->(SleeplessInSeattle_R),
  (RitaW)-[:ACTED_IN {roles:['Suzy']}]->(SleeplessInSeattle_R),
  (BillPull)-[:ACTED_IN {roles:['Walter']}]->(SleeplessInSeattle_R),
  (VictorG)-[:ACTED_IN {roles:['Greg']}]->(SleeplessInSeattle_R),
  (RosieO)-[:ACTED_IN {roles:['Becky']}]->(SleeplessInSeattle_R),
  (NoraE)-[:DIRECTED]->(SleeplessInSeattle_R)

CREATE (JoeVersustheVolcano:Movie:INSTANCE {title:'Joe Versus the Volcano', released:1990, tagline:'A story of love, lava and burning desire.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JoeVersustheVolcano_R:Movie_R:RESOURCE {id: 'JoeVersustheVolcano'})
CREATE (JohnS:Person:INSTANCE {name:'John Patrick Stanley', born:1950})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JohnS_R:Person_R:RESOURCE {id: 'JohnS'})
CREATE (Nathan:Person:INSTANCE {name:'Nathan Lane', born:1956})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Nathan_R:Person_R:RESOURCE {id: 'Nathan'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Joe Banks']}]->(JoeVersustheVolcano_R),
  (MegR)-[:ACTED_IN {roles:['DeDe', 'Angelica Graynamore', 'Patricia Graynamore']}]->(JoeVersustheVolcano_R),
  (Nathan)-[:ACTED_IN {roles:['Baw']}]->(JoeVersustheVolcano_R),
  (JohnS)-[:DIRECTED]->(JoeVersustheVolcano_R)

CREATE (WhenHarryMetSally:Movie:INSTANCE {title:'When Harry Met Sally', released:1998, tagline:'At odds in life... in love on-line.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(WhenHarryMetSally_R:Movie_R:RESOURCE {id: 'WhenHarryMetSally'})
CREATE (BillyC:Person:INSTANCE {name:'Billy Crystal', born:1948})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BillyC_R:Person_R:RESOURCE {id: 'BillyC'})
CREATE (CarrieF:Person:INSTANCE {name:'Carrie Fisher', born:1956})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CarrieF_R:Person_R:RESOURCE {id: 'CarrieF'})
CREATE (BrunoK:Person:INSTANCE {name:'Bruno Kirby', born:1949})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BrunoK_R:Person_R:RESOURCE {id: 'BrunoK'})
CREATE
  (BillyC)-[:ACTED_IN {roles:['Harry Burns']}]->(WhenHarryMetSally_R),
  (MegR)-[:ACTED_IN {roles:['Sally Albright']}]->(WhenHarryMetSally_R),
  (CarrieF)-[:ACTED_IN {roles:['Marie']}]->(WhenHarryMetSally_R),
  (BrunoK)-[:ACTED_IN {roles:['Jess']}]->(WhenHarryMetSally_R),
  (RobR)-[:DIRECTED]->(WhenHarryMetSally_R),
  (RobR)-[:PRODUCED]->(WhenHarryMetSally_R),
  (NoraE)-[:PRODUCED]->(WhenHarryMetSally_R),
  (NoraE)-[:WROTE]->(WhenHarryMetSally_R)

CREATE (ThatThingYouDo:Movie:INSTANCE {title:'That Thing You Do', released:1996, tagline:'In every life there comes a time when that thing you dream becomes that thing you do'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ThatThingYouDo_R:Movie_R:RESOURCE {id: 'ThatThingYouDo'})
CREATE (LivT:Person:INSTANCE {name:'Liv Tyler', born:1977})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(LivT_R:Person_R:RESOURCE {id: 'LivT'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Mr. White']}]->(ThatThingYouDo_R),
  (LivT)-[:ACTED_IN {roles:['Faye Dolan']}]->(ThatThingYouDo_R),
  (Charlize)-[:ACTED_IN {roles:['Tina']}]->(ThatThingYouDo_R),
  (TomH)-[:DIRECTED]->(ThatThingYouDo_R)

CREATE (TheReplacements:Movie:INSTANCE {title:'The Replacements', released:2000, tagline:'Pain heals, Chicks dig scars... Glory lasts forever'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheReplacements_R:Movie_R:RESOURCE {id: 'TheReplacements'})
CREATE (Brooke:Person:INSTANCE {name:'Brooke Langton', born:1970})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Brooke_R:Person_R:RESOURCE {id: 'Brooke'})
CREATE (Gene:Person:INSTANCE {name:'Gene Hackman', born:1930})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Gene_R:Person_R:RESOURCE {id: 'Gene'})
CREATE (Orlando:Person:INSTANCE {name:'Orlando Jones', born:1968})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Orlando_R:Person_R:RESOURCE {id: 'Orlando'})
CREATE (Howard:Person:INSTANCE {name:'Howard Deutch', born:1950})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Howard_R:Person_R:RESOURCE {id: 'Howard'})
CREATE
  (Keanu)-[:ACTED_IN {roles:['Shane Falco']}]->(TheReplacements_R),
  (Brooke)-[:ACTED_IN {roles:['Annabelle Farrell']}]->(TheReplacements_R),
  (Gene)-[:ACTED_IN {roles:['Jimmy McGinty']}]->(TheReplacements_R),
  (Orlando)-[:ACTED_IN {roles:['Clifford Franklin']}]->(TheReplacements_R),
  (Howard)-[:DIRECTED]->(TheReplacements_R)

CREATE (RescueDawn:Movie:INSTANCE {title:'RescueDawn', released:2006, tagline:"Based on the extraordinary true story of one man's fight for freedom"})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RescueDawn_R:Movie_R:RESOURCE {id: 'RescueDawn'})
CREATE (ChristianB:Person:INSTANCE {name:'Christian Bale', born:1974})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ChristianB_R:Person_R:RESOURCE {id: 'ChristianB'})
CREATE (ZachG:Person:INSTANCE {name:'Zach Grenier', born:1954})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ZachG_R:Person_R:RESOURCE {id: 'ZachG'})
CREATE
  (MarshallB)-[:ACTED_IN {roles:['Admiral']}]->(RescueDawn_R),
  (ChristianB)-[:ACTED_IN {roles:['Dieter Dengler']}]->(RescueDawn_R),
  (ZachG)-[:ACTED_IN {roles:['Squad Leader']}]->(RescueDawn_R),
  (SteveZ)-[:ACTED_IN {roles:['Duane']}]->(RescueDawn_R),
  (WernerH)-[:DIRECTED]->(RescueDawn_R)

CREATE (TheBirdcage:Movie:INSTANCE {title:'The Birdcage', released:1996, tagline:'Come as you are'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheBirdcage_R:Movie_R:RESOURCE {id: 'TheBirdcage'})
CREATE (MikeN:Person:INSTANCE {name:'Mike Nichols', born:1931})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MikeN_R:Person_R:RESOURCE {id: 'MikeN'})
CREATE
  (Robin)-[:ACTED_IN {roles:['Armand Goldman']}]->(TheBirdcage_R),
  (Nathan)-[:ACTED_IN {roles:['Albert Goldman']}]->(TheBirdcage_R),
  (Gene)-[:ACTED_IN {roles:['Sen. Kevin Keeley']}]->(TheBirdcage_R),
  (MikeN)-[:DIRECTED]->(TheBirdcage_R)

CREATE (Unforgiven:Movie:INSTANCE {title:'Unforgiven', released:1992, tagline:"It's a hell of a thing, killing a man"})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Unforgiven_R:Movie_R:RESOURCE {id: 'Unforgiven'})
CREATE (RichardH:Person:INSTANCE {name:'Richard Harris', born:1930})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RichardH_R:Person_R:RESOURCE {id: 'RichardH'})
CREATE (ClintE:Person:INSTANCE {name:'Clint Eastwood', born:1930})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ClintE_R:Person_R:RESOURCE {id: 'ClintE'})
CREATE
  (RichardH)-[:ACTED_IN {roles:['English Bob']}]->(Unforgiven_R),
  (ClintE)-[:ACTED_IN {roles:['Bill Munny']}]->(Unforgiven_R),
  (Gene)-[:ACTED_IN {roles:['Little Bill Daggett']}]->(Unforgiven_R),
  (ClintE)-[:DIRECTED]->(Unforgiven_R)

CREATE (JohnnyMnemonic:Movie:INSTANCE {title:'Johnny Mnemonic', released:1995, tagline:'The hottest data on earth. In the coolest head in town'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JohnnyMnemonic_R:Movie_R:RESOURCE {id: 'JohnnyMnemonic'})
CREATE (Takeshi:Person:INSTANCE {name:'Takeshi Kitano', born:1947})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Takeshi_R:Person_R:RESOURCE {id: 'Takeshi'})
CREATE (Dina:Person:INSTANCE {name:'Dina Meyer', born:1968})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Dina_R:Person_R:RESOURCE {id: 'Dina'})
CREATE (IceT:Person:INSTANCE {name:'Ice-T', born:1958})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(IceT_R:Person_R:RESOURCE {id: 'IceT'})
CREATE (RobertL:Person:INSTANCE {name:'Robert Longo', born:1953})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RobertL_R:Person_R:RESOURCE {id: 'RobertL'})
CREATE
  (Keanu)-[:ACTED_IN {roles:['Johnny Mnemonic']}]->(JohnnyMnemonic_R),
  (Takeshi)-[:ACTED_IN {roles:['Takahashi']}]->(JohnnyMnemonic_R),
  (Dina)-[:ACTED_IN {roles:['Jane']}]->(JohnnyMnemonic_R),
  (IceT)-[:ACTED_IN {roles:['J-Bone']}]->(JohnnyMnemonic_R),
  (RobertL)-[:DIRECTED]->(JohnnyMnemonic_R)

CREATE (CloudAtlas:Movie:INSTANCE {title:'Cloud Atlas', released:2012, tagline:'Everything is connected'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CloudAtlas_R:Movie_R:RESOURCE {id: 'CloudAtlas'})
CREATE (HalleB:Person:INSTANCE {name:'Halle Berry', born:1966})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(HalleB_R:Person_R:RESOURCE {id: 'HalleB'})
CREATE (JimB:Person:INSTANCE {name:'Jim Broadbent', born:1949})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JimB_R:Person_R:RESOURCE {id: 'JimB'})
CREATE (TomT:Person:INSTANCE {name:'Tom Tykwer', born:1965})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TomT_R:Person_R:RESOURCE {id: 'TomT'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Zachry', 'Dr. Henry Goose', 'Isaac Sachs', 'Dermot Hoggins']}]->(CloudAtlas_R),
  (Hugo)-[:ACTED_IN {roles:['Bill Smoke', 'Haskell Moore', 'Tadeusz Kesselring', 'Nurse Noakes', 'Boardman Mephi', 'Old Georgie']}]->(CloudAtlas_R),
  (HalleB)-[:ACTED_IN {roles:['Luisa Rey', 'Jocasta Ayrs', 'Ovid', 'Meronym']}]->(CloudAtlas_R),
  (JimB)-[:ACTED_IN {roles:['Vyvyan Ayrs', 'Captain Molyneux', 'Timothy Cavendish']}]->(CloudAtlas_R),
  (TomT)-[:DIRECTED]->(CloudAtlas_R),
  (AndyW)-[:DIRECTED]->(CloudAtlas_R),
  (LanaW)-[:DIRECTED]->(CloudAtlas_R)

CREATE (TheDaVinciCode:Movie:INSTANCE {title:'The Da Vinci Code', released:2006, tagline:'Break The Codes'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheDaVinciCode_R:Movie_R:RESOURCE {id: 'TheDaVinciCode'})
CREATE (IanM:Person:INSTANCE {name:'Ian McKellen', born:1939})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(IanM_R:Person_R:RESOURCE {id: 'IanM'})
CREATE (AudreyT:Person:INSTANCE {name:'Audrey Tautou', born:1976})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AudreyT_R:Person_R:RESOURCE {id: 'AudreyT'})
CREATE (PaulB:Person:INSTANCE {name:'Paul Bettany', born:1971})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(PaulB_R:Person_R:RESOURCE {id: 'PaulB'})
CREATE (RonH:Person:INSTANCE {name:'Ron Howard', born:1954})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RonH_R:Person_R:RESOURCE {id: 'RonH'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Dr. Robert Langdon']}]->(TheDaVinciCode_R),
  (IanM)-[:ACTED_IN {roles:['Sir Leight Teabing']}]->(TheDaVinciCode_R),
  (AudreyT)-[:ACTED_IN {roles:['Sophie Neveu']}]->(TheDaVinciCode_R),
  (PaulB)-[:ACTED_IN {roles:['Silas']}]->(TheDaVinciCode_R),
  (RonH)-[:DIRECTED]->(TheDaVinciCode_R)

CREATE (VforVendetta:Movie:INSTANCE {title:'V for Vendetta', released:2006, tagline:'Freedom! Forever!'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(VforVendetta_R:Movie_R:RESOURCE {id: 'VforVendetta'})
CREATE (NatalieP:Person:INSTANCE {name:'Natalie Portman', born:1981})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(NatalieP_R:Person_R:RESOURCE {id: 'NatalieP'})
CREATE (StephenR:Person:INSTANCE {name:'Stephen Rea', born:1946})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(StephenR_R:Person_R:RESOURCE {id: 'StephenR'})
CREATE (JohnH:Person:INSTANCE {name:'John Hurt', born:1940})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JohnH_R:Person_R:RESOURCE {id: 'JohnH'})
CREATE (BenM:Person:INSTANCE {name: 'Ben Miles', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BenM_R:Person_R:RESOURCE {id: 'BenM'})
CREATE
  (Hugo)-[:ACTED_IN {roles:['V']}]->(VforVendetta_R),
  (NatalieP)-[:ACTED_IN {roles:['Evey Hammond']}]->(VforVendetta_R),
  (StephenR)-[:ACTED_IN {roles:['Eric Finch']}]->(VforVendetta_R),
  (JohnH)-[:ACTED_IN {roles:['High Chancellor Adam Sutler']}]->(VforVendetta_R),
  (BenM)-[:ACTED_IN {roles:['Dascomb']}]->(VforVendetta_R),
  (JamesM)-[:DIRECTED]->(VforVendetta_R),
  (AndyW)-[:PRODUCED]->(VforVendetta_R),
  (LanaW)-[:PRODUCED]->(VforVendetta_R),
  (JoelS)-[:PRODUCED]->(VforVendetta_R),
  (AndyW)-[:WROTE]->(VforVendetta_R),
  (LanaW)-[:WROTE]->(VforVendetta_R)

CREATE (SpeedRacer:Movie:INSTANCE {title:'Speed Racer', released:2008, tagline:'Speed has no limits'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SpeedRacer_R:Movie_R:RESOURCE {id: 'SpeedRacer'})
CREATE (EmileH:Person:INSTANCE {name:'Emile Hirsch', born:1985})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(EmileH_R:Person_R:RESOURCE {id: 'EmileH'})
CREATE (JohnG:Person:INSTANCE {name:'John Goodman', born:1960})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JohnG_R:Person_R:RESOURCE {id: 'JohnG'})
CREATE (SusanS:Person:INSTANCE {name:'Susan Sarandon', born:1946})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SusanS_R:Person_R:RESOURCE {id: 'SusanS'})
CREATE (MatthewF:Person:INSTANCE {name:'Matthew Fox', born:1966})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MatthewF_R:Person_R:RESOURCE {id: 'MatthewF'})
CREATE (ChristinaR:Person:INSTANCE {name:'Christina Ricci', born:1980})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ChristinaR_R:Person_R:RESOURCE {id: 'ChristinaR'})
CREATE (Rain:Person:INSTANCE {name:'Rain', born:1982})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Rain_R:Person_R:RESOURCE {id: 'Rain'})
CREATE
  (EmileH)-[:ACTED_IN {roles:['Speed Racer']}]->(SpeedRacer_R),
  (JohnG)-[:ACTED_IN {roles:['Pops']}]->(SpeedRacer_R),
  (SusanS)-[:ACTED_IN {roles:['Mom']}]->(SpeedRacer_R),
  (MatthewF)-[:ACTED_IN {roles:['Racer X']}]->(SpeedRacer_R),
  (ChristinaR)-[:ACTED_IN {roles:['Trixie']}]->(SpeedRacer_R),
  (Rain)-[:ACTED_IN {roles:['Taejo Togokahn']}]->(SpeedRacer_R),
  (BenM)-[:ACTED_IN {roles:['Cass Jones']}]->(SpeedRacer_R),
  (AndyW)-[:DIRECTED]->(SpeedRacer_R),
  (LanaW)-[:DIRECTED]->(SpeedRacer_R),
  (AndyW)-[:WROTE]->(SpeedRacer_R),
  (LanaW)-[:WROTE]->(SpeedRacer_R),
  (JoelS)-[:PRODUCED]->(SpeedRacer_R)

CREATE (NinjaAssassin:Movie:INSTANCE {title:'Ninja Assassin', released:2009, tagline:'Prepare to enter a secret world of assassins'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(NinjaAssassin_R:Movie_R:RESOURCE {id: 'NinjaAssassin'})
CREATE (NaomieH:Person:INSTANCE {name:'Naomie Harris'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(NaomieH_R:Person_R:RESOURCE {id: 'NaomieH'})
CREATE
  (Rain)-[:ACTED_IN {roles:['Raizo']}]->(NinjaAssassin_R),
  (NaomieH)-[:ACTED_IN {roles:['Mika Coretti']}]->(NinjaAssassin_R),
  (RickY)-[:ACTED_IN {roles:['Takeshi']}]->(NinjaAssassin_R),
  (BenM)-[:ACTED_IN {roles:['Ryan Maslow']}]->(NinjaAssassin_R),
  (JamesM)-[:DIRECTED]->(NinjaAssassin_R),
  (AndyW)-[:PRODUCED]->(NinjaAssassin_R),
  (LanaW)-[:PRODUCED]->(NinjaAssassin_R),
  (JoelS)-[:PRODUCED]->(NinjaAssassin_R)

CREATE (TheGreenMile:Movie:INSTANCE {title:'The Green Mile', released:1999, tagline:"Walk a mile you'll never forget."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(TheGreenMile_R:Movie_R:RESOURCE {id: 'TheGreenMile'})
CREATE (MichaelD:Person:INSTANCE {name:'Michael Clarke Duncan', born:1957})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MichaelD_R:Person_R:RESOURCE {id: 'MichaelD'})
CREATE (DavidM:Person:INSTANCE {name:'David Morse', born:1953})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(DavidM_R:Person_R:RESOURCE {id: 'DavidM'})
CREATE (SamR:Person:INSTANCE {name:'Sam Rockwell', born:1968})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SamR_R:Person_R:RESOURCE {id: 'SamR'})
CREATE (GaryS:Person:INSTANCE {name:'Gary Sinise', born:1955})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(GaryS_R:Person_R:RESOURCE {id: 'GaryS'})
CREATE (PatriciaC:Person:INSTANCE {name:'Patricia Clarkson', born:1959})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(PatriciaC_R:Person_R:RESOURCE {id: 'PatriciaC'})
CREATE (FrankD:Person:INSTANCE {name:'Frank Darabont', born:1959})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(FrankD_R:Person_R:RESOURCE {id: 'FrankD'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Paul Edgecomb']}]->(TheGreenMile_R),
  (MichaelD)-[:ACTED_IN {roles:['John Coffey']}]->(TheGreenMile_R),
  (DavidM)-[:ACTED_IN {roles:['Brutus "Brutal" Howell']}]->(TheGreenMile_R),
  (BonnieH)-[:ACTED_IN {roles:['Jan Edgecomb']}]->(TheGreenMile_R),
  (JamesC)-[:ACTED_IN {roles:['Warden Hal Moores']}]->(TheGreenMile_R),
  (SamR)-[:ACTED_IN {roles:['"Wild Bill" Wharton']}]->(TheGreenMile_R),
  (GaryS)-[:ACTED_IN {roles:['Burt Hammersmith']}]->(TheGreenMile_R),
  (PatriciaC)-[:ACTED_IN {roles:['Melinda Moores']}]->(TheGreenMile_R),
  (FrankD)-[:DIRECTED]->(TheGreenMile_R)

CREATE (FrostNixon:Movie:INSTANCE {title:'Frost/Nixon', released:2008, tagline:'400 million people were waiting for the truth.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(FrostNixon_R:Movie_R:RESOURCE {id: 'FrostNixon'})
CREATE (FrankL:Person:INSTANCE {name:'Frank Langella', born:1938})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(FrankL_R:Person_R:RESOURCE {id: 'FrankL'})
CREATE (MichaelS:Person:INSTANCE {name:'Michael Sheen', born:1969})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MichaelS_R:Person_R:RESOURCE {id: 'MichaelS'})
CREATE (OliverP:Person:INSTANCE {name:'Oliver Platt', born:1960})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(OliverP_R:Person_R:RESOURCE {id: 'OliverP'})
CREATE
  (FrankL)-[:ACTED_IN {roles:['Richard Nixon']}]->(FrostNixon_R),
  (MichaelS)-[:ACTED_IN {roles:['David Frost']}]->(FrostNixon_R),
  (KevinB)-[:ACTED_IN {roles:['Jack Brennan']}]->(FrostNixon_R),
  (OliverP)-[:ACTED_IN {roles:['Bob Zelnick']}]->(FrostNixon_R),
  (SamR)-[:ACTED_IN {roles:['James Reston, Jr.']}]->(FrostNixon_R),
  (RonH)-[:DIRECTED]->(FrostNixon_R)

CREATE (Hoffa:Movie:INSTANCE {title:'Hoffa', released:1992, tagline:"He didn't want law. He wanted justice."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Hoffa_R:Movie_R:RESOURCE {id: 'Hoffa'})
CREATE (DannyD:Person:INSTANCE {name:'Danny DeVito', born:1944})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(DannyD_R:Person_R:RESOURCE {id: 'DannyD'})
CREATE (JohnR:Person:INSTANCE {name:'John C. Reilly', born:1965})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JohnR_R:Person_R:RESOURCE {id: 'JohnR'})
CREATE
  (JackN)-[:ACTED_IN {roles:['Hoffa']}]->(Hoffa_R),
  (DannyD)-[:ACTED_IN {roles:['Robert "Bobby" Ciaro']}]->(Hoffa_R),
  (JTW)-[:ACTED_IN {roles:['Frank Fitzsimmons']}]->(Hoffa_R),
  (JohnR)-[:ACTED_IN {roles:['Peter "Pete" Connelly']}]->(Hoffa_R),
  (DannyD)-[:DIRECTED]->(Hoffa_R)

CREATE (Apollo13:Movie:INSTANCE {title:'Apollo 13', released:1995, tagline:'Houston, we have a problem.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Apollo13_R:Movie_R:RESOURCE {id: 'Apollo13'})
CREATE (EdH:Person:INSTANCE {name:'Ed Harris', born:1950})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(EdH_R:Person_R:RESOURCE {id: 'EdH'})
CREATE (BillPax:Person:INSTANCE {name:'Bill Paxton', born:1955})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BillPax_R:Person_R:RESOURCE {id: 'BillPax'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Jim Lovell']}]->(Apollo13_R),
  (KevinB)-[:ACTED_IN {roles:['Jack Swigert']}]->(Apollo13_R),
  (EdH)-[:ACTED_IN {roles:['Gene Kranz']}]->(Apollo13_R),
  (BillPax)-[:ACTED_IN {roles:['Fred Haise']}]->(Apollo13_R),
  (GaryS)-[:ACTED_IN {roles:['Ken Mattingly']}]->(Apollo13_R),
  (RonH)-[:DIRECTED]->(Apollo13_R)

CREATE (Twister:Movie:INSTANCE {title:'Twister', released:1996, tagline:"Don't Breathe. Don't Look Back."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Twister_R:Movie_R:RESOURCE {id: 'Twister'})
CREATE (PhilipH:Person:INSTANCE {name:'Philip Seymour Hoffman', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(PhilipH_R:Person_R:RESOURCE {id: 'PhilipH'})
CREATE (JanB:Person:INSTANCE {name:'Jan de Bont', born:1943})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JanB_R:Person_R:RESOURCE {id: 'JanB'})
CREATE
  (BillPax)-[:ACTED_IN {roles:['Bill Harding']}]->(Twister_R),
  (HelenH)-[:ACTED_IN {roles:['Dr. Jo Harding']}]->(Twister_R),
  (ZachG)-[:ACTED_IN {roles:['Eddie']}]->(Twister_R),
  (PhilipH)-[:ACTED_IN {roles:['Dustin "Dusty" Davis']}]->(Twister_R),
  (JanB)-[:DIRECTED]->(Twister_R)

CREATE (CastAway:Movie:INSTANCE {title:'Cast Away', released:2000, tagline:'At the edge of the world, his journey begins.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CastAway_R:Movie_R:RESOURCE {id: 'CastAway'})
CREATE (RobertZ:Person:INSTANCE {name:'Robert Zemeckis', born:1951})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(RobertZ_R:Person_R:RESOURCE {id: 'RobertZ'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Chuck Noland']}]->(CastAway_R),
  (HelenH)-[:ACTED_IN {roles:['Kelly Frears']}]->(CastAway_R),
  (RobertZ)-[:DIRECTED]->(CastAway_R)

CREATE (OneFlewOvertheCuckoosNest:Movie:INSTANCE {title:"One Flew Over the Cuckoo's Nest", released:1975, tagline:"If he's crazy, what does that make you?"})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(OneFlewOvertheCuckoosNest_R:Movie_R:RESOURCE {id: 'OneFlewOvertheCuckoosNest'})
CREATE (MilosF:Person:INSTANCE {name:'Milos Forman', born:1932})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(MilosF_R:Person_R:RESOURCE {id: 'MilosF'})
CREATE
  (JackN)-[:ACTED_IN {roles:['Randle McMurphy']}]->(OneFlewOvertheCuckoosNest_R),
  (DannyD)-[:ACTED_IN {roles:['Martini']}]->(OneFlewOvertheCuckoosNest_R),
  (MilosF)-[:DIRECTED]->(OneFlewOvertheCuckoosNest_R)

CREATE (SomethingsGottaGive:Movie:INSTANCE {title:"Something's Gotta Give", released:2003})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(SomethingsGottaGive_R:Movie_R:RESOURCE {id: 'SomethingsGottaGive'})
CREATE (DianeK:Person:INSTANCE {name:'Diane Keaton', born:1946})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(DianeK_R:Person_R:RESOURCE {id: 'DianeK'})
CREATE (NancyM:Person:INSTANCE {name:'Nancy Meyers', born:1949})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(NancyM_R:Person_R:RESOURCE {id: 'NancyM'})
CREATE
  (JackN)-[:ACTED_IN {roles:['Harry Sanborn']}]->(SomethingsGottaGive_R),
  (DianeK)-[:ACTED_IN {roles:['Erica Barry']}]->(SomethingsGottaGive_R),
  (Keanu)-[:ACTED_IN {roles:['Julian Mercer']}]->(SomethingsGottaGive_R),
  (NancyM)-[:DIRECTED]->(SomethingsGottaGive_R),
  (NancyM)-[:PRODUCED]->(SomethingsGottaGive_R),
  (NancyM)-[:WROTE]->(SomethingsGottaGive_R)

CREATE (BicentennialMan:Movie:INSTANCE {title:'Bicentennial Man', released:1999, tagline:"One robot's 200 year journey to become an ordinary man."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(BicentennialMan_R:Movie_R:RESOURCE {id: 'BicentennialMan'})
CREATE (ChrisC:Person:INSTANCE {name:'Chris Columbus', born:1958})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ChrisC_R:Person_R:RESOURCE {id: 'ChrisC'})
CREATE
  (Robin)-[:ACTED_IN {roles:['Andrew Marin']}]->(BicentennialMan_R),
  (OliverP)-[:ACTED_IN {roles:['Rupert Burns']}]->(BicentennialMan_R),
  (ChrisC)-[:DIRECTED]->(BicentennialMan_R)

CREATE (CharlieWilsonsWar:Movie:INSTANCE {title:"Charlie Wilson's War", released:2007, tagline:"A stiff drink. A little mascara. A lot of nerve. Who said they couldn't bring down the Soviet empire."})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(CharlieWilsonsWar_R:Movie_R:RESOURCE {id: 'CharlieWilsonsWar'})
CREATE (JuliaR:Person:INSTANCE {name:'Julia Roberts', born:1967})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JuliaR_R:Person_R:RESOURCE {id: 'JuliaR'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Rep. Charlie Wilson']}]->(CharlieWilsonsWar_R),
  (JuliaR)-[:ACTED_IN {roles:['Joanne Herring']}]->(CharlieWilsonsWar_R),
  (PhilipH)-[:ACTED_IN {roles:['Gust Avrakotos']}]->(CharlieWilsonsWar_R),
  (MikeN)-[:DIRECTED]->(CharlieWilsonsWar_R)

CREATE (ThePolarExpress:Movie:INSTANCE {title:'The Polar Express', released:2004, tagline:'This Holiday Season… Believe'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ThePolarExpress_R:Movie_R:RESOURCE {id: 'ThePolarExpress'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Hero Boy', 'Father', 'Conductor', 'Hobo', 'Scrooge', 'Santa Claus']}]->(ThePolarExpress_R),
  (RobertZ)-[:DIRECTED]->(ThePolarExpress_R)

CREATE (ALeagueofTheirOwn:Movie:INSTANCE {title:'A League of Their Own', released:1992, tagline:'Once in a lifetime you get a chance to do something different.'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(ALeagueofTheirOwn_R:Movie_R:RESOURCE {id: 'ALeagueofTheirOwn'})
CREATE (Madonna:Person:INSTANCE {name:'Madonna', born:1954})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(Madonna_R:Person_R:RESOURCE {id: 'Madonna'})
CREATE (GeenaD:Person:INSTANCE {name:'Geena Davis', born:1956})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(GeenaD_R:Person_R:RESOURCE {id: 'GeenaD'})
CREATE (LoriP:Person:INSTANCE {name:'Lori Petty', born:1963})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(LoriP_R:Person_R:RESOURCE {id: 'LoriP'})
CREATE (PennyM:Person:INSTANCE {name:'Penny Marshall', born:1943})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(PennyM_R:Person_R:RESOURCE {id: 'PennyM'})
CREATE
  (TomH)-[:ACTED_IN {roles:['Jimmy Dugan']}]->(ALeagueofTheirOwn_R),
  (GeenaD)-[:ACTED_IN {roles:['Dottie Hinson']}]->(ALeagueofTheirOwn_R),
  (LoriP)-[:ACTED_IN {roles:['Kit Keller']}]->(ALeagueofTheirOwn_R),
  (RosieO)-[:ACTED_IN {roles:['Doris Murphy']}]->(ALeagueofTheirOwn_R),
  (Madonna)-[:ACTED_IN {roles:['"All the Way" Mae Mordabito']}]->(ALeagueofTheirOwn_R),
  (BillPax)-[:ACTED_IN {roles:['Bob Hinson']}]->(ALeagueofTheirOwn_R),
  (PennyM)-[:DIRECTED]->(ALeagueofTheirOwn_R)

CREATE (PaulBlythe:Person:INSTANCE {name:'Paul Blythe'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(PaulBlythe_R:Person_R:RESOURCE {id: 'PaulBlythe'})
CREATE (AngelaScope:Person:INSTANCE {name:'Angela Scope'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(AngelaScope_R:Person_R:RESOURCE {id: 'AngelaScope'})
CREATE (JessicaThompson:Person:INSTANCE {name:'Jessica Thompson'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JessicaThompson_R:Person_R:RESOURCE {id: 'JessicaThompson'})
CREATE (JamesThompson:Person:INSTANCE {name:'James Thompson'})-[:VERSION_OF {from: datetime('2020-01-01T00:00:00.000+01:00[Europe/Oslo]').epochMillis}]->(JamesThompson_R:Person_R:RESOURCE {id: 'JamesThompson'})

CREATE
  (JamesThompson)-[:FOLLOWS]->(JessicaThompson_R),
  (AngelaScope)-[:FOLLOWS]->(JessicaThompson_R),
  (PaulBlythe)-[:FOLLOWS]->(AngelaScope_R)

CREATE
  (JessicaThompson)-[:REVIEWED {summary:'An amazing journey', rating:95}]->(CloudAtlas_R),
  (JessicaThompson)-[:REVIEWED {summary:'Silly, but fun', rating:65}]->(TheReplacements_R),
  (JamesThompson)-[:REVIEWED {summary:'The coolest football movie ever', rating:100}]->(TheReplacements_R),
  (AngelaScope)-[:REVIEWED {summary:'Pretty funny at times', rating:62}]->(TheReplacements_R),
  (JessicaThompson)-[:REVIEWED {summary:'Dark, but compelling', rating:85}]->(Unforgiven_R),
  (JessicaThompson)-[:REVIEWED {summary:"Slapstick redeemed only by the Robin Williams and Gene Hackman's stellar performances", rating:45}]->(TheBirdcage_R),
  (JessicaThompson)-[:REVIEWED {summary:'A solid romp', rating:68}]->(TheDaVinciCode_R),
  (JamesThompson)-[:REVIEWED {summary:'Fun, but a little far fetched', rating:65}]->(TheDaVinciCode_R)
