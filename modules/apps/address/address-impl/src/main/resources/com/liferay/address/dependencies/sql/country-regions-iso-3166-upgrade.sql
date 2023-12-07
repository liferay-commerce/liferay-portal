UPDATE Region SET regionCode = '75.0' WHERE regionCode = '75c' AND name ='Paris';
UPDATE CIWarehouse SET commerceRegionCode = '75.0' WHERE commerceRegionCode = '75C' AND countryTwoLettersISOCode = 'FR';

UPDATE Region SET regionCode = 'CMX' WHERE regionCode = 'DIF' AND name ='Ciudad de México';
UPDATE CIWarehouse SET commerceRegionCode = 'CMX' WHERE commerceRegionCode = 'DIF' and countryTwoLettersISOCode = 'MX';

DELETE FROM Region WHERE regionCode = 'NTH' AND countryId IN (SELECT countryId FROM Country WHERE a2 = 'GB');