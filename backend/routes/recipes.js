const express = require('express');
const router = express.Router();
const GoogleImages = require('google-images');
const moment = require('moment');
const request = require('request');
const async = require('async');

const imgSearchOptions = {size: 'large', dominantColor: 'brown'};

let lastSyncedTime = null;
let nextSyncTime = null;
let googleImgClient = new GoogleImages(process.env.GOOGLE_CSE_ID, process.env.GOOGLE_API_KEY);
let recipesData = [];

/* GET recipes listing. */
router.get('/', function(req, res, next) {
    if (recipesData.length === 0  ||
        lastSyncedTime === null || moment().isSameOrAfter(nextSyncTime)) {
        request(process.env.UDACITY_BAKING_URL, function (err, resp, uRecipes) {
            console.log('Starting a cache refresh ...');

            // Decode the Json recipes
            uRecipes = JSON.parse(uRecipes);
            console.log(`Fetched ${uRecipes.length} recipes from Udacity upstream source.`);

            // Go over all retrieved results and see which one to cache and which one to discard
            async.each(uRecipes, function(uRecipe, uRecipeClb) {
                async.filter(recipesData, function(storedRecipe, stRecipeClb) {
                    stRecipeClb(null, storedRecipe.id !== undefined && storedRecipe.id === uRecipe.id);
                }, function (err, storedRecipeResults) {
                    if (storedRecipeResults.length === 0) {
                        // First time we see this recipe. See if it has an image, otherwise use a Google provided one
                        if (uRecipe.image.length === 0) {
                            // nope. No image available. Google, can you help?
                            googleImgClient.search(uRecipe.name, imgSearchOptions)
                                .then(gRecipeImages => {
                                    // Just pick any image from the search results. It should be fine
                                    if (gRecipeImages.length !== 0) {
                                        uRecipe.image = gRecipeImages[parseInt(Math.random() * gRecipeImages.length)].url;
                                    } else {
                                        console.warn(`Couldn't find any Google images for recipe '${uRecipe.name}'`);
                                    }
                                    recipesData.push(uRecipe);
                                    uRecipeClb();
                                });
                        } else {
                            // image is already present in Udacity recipe. Cache the Json recipe and move on
                            recipesData.push(uRecipe);
                            uRecipeClb();
                        }
                    } else {
                        // We already know about this recipe. Pass
                        uRecipeClb();
                    }
                });
            }, function(err) {
                // Everything has been processed
                if (err) throw err;

                console.log('Cache successfully refreshed. Sending data ...');
                res.send(recipesData);
            });

            // Timestamp this action for cache purposes
            lastSyncedTime = moment();
            nextSyncTime = moment().add(15, 'minutes');
        });
    } else {
        // We can just retrieved the cached recipe since no update is required
        res.send(recipesData);
    }
});

module.exports = router;
