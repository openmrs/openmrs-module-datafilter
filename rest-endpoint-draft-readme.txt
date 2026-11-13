-- create a mapping:

Request type: POST
Request URL: {base_rest_url}/datafilter/entitybasismap
sample payload:
1. Map user to a location
{
"entityIdentifier": "user-uuid", -- replace accordingly
"entityType":"org.openmrs.User",
"basisIdentifier":"location-uuid", -- replace accordingly
"basisType":"org.openmrs.Location"
}

2. Map patient to a location
{
"entityIdentifier": "patient-uuid", -- replace accordingly
"entityType":"org.openmrs.User",
"basisIdentifier":"location-uuid", -- replace accordingly
"basisType":"org.openmrs.Location"
}
-- -------------------------


-- searching for locations for a user
Request type: POST
Request URL: {base_rest_url}/datafilter/search
Payload:
{
"entityIdentifier": "46e679ea-74ee-45f1-9cad-4b7981852892",
"entityType":"org.openmrs.User",
"basisIdentifier":"",
"basisType":"org.openmrs.Location"
}

sample response:
[
    {
        "uuid": "e74e9407-21c0-11ef-a960-629a87b59084",
        "dateCreated": 1717429786000,
        "entity": {
            "name": "Jelly Kwacha",
            "uuid": "61686921-374c-4ce1-bab9-270ef9c0eeb9"
        },
        "basis": {
            "name": "Railways Dispensary (Kisumu)",
            "uuid": "fc4aeac6-24e7-4da1-8d51-9fc1997a80d4"
        },
        "creator": {
            "name": "Super User",
            "uuid": "54dc32bc-eaea-11e2-90be-0800271ad0ce"
        }
    }
]


-- Search patients mapped to a location

Request type: POST
Request URL: {base_rest_url}/datafilter/search

Payload:
{
"entityIdentifier": "",
"entityType":"org.openmrs.Patient",
"basisIdentifier":"fc4aeac6-24e7-4da1-8d51-9fc1997a80d4",
"basisType":"org.openmrs.Location"
}

Sample response:

[
    {
        "uuid": "d5242512-21c1-11ef-a960-629a87b59084",
        "dateCreated": 1717430186000,
        "entity": {
            "OpenMRSID": "MXFXD",
            "identifier": "MXFXD",
            "gender": "F",
            "PatientClinicNumber": "2467/24",
            "name": "Aubry Waso Makena",
            "uuid": "d97d2005-528c-4952-87e7-eeadf9d0628b",
            "age": 10
        },
        "basis": {
            "name": "Railways Dispensary (Kisumu)",
            "uuid": "fc4aeac6-24e7-4da1-8d51-9fc1997a80d4"
        },
        "creator": {
            "name": "Super User",
            "uuid": "54dc32bc-eaea-11e2-90be-0800271ad0ce"
        }
    }
]
