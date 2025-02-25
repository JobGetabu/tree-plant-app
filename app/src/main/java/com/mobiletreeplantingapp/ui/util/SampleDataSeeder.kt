package com.mobiletreeplantingapp.ui.util

import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.data.model.ArticleCategory
import kotlinx.coroutines.tasks.await

object SampleDataSeeder {
    private val firestore = FirebaseFirestore.getInstance()
    private val articlesCollection = firestore.collection("articles")

    suspend fun seedSampleArticles() {
        val sampleArticles = listOf(
            hashMapOf(
                "title" to "The Importance of Urban Trees",
                "content" to """
                   #Trees in cities provide numerous benefits to our communities:

                    - Improve air quality by filtering pollutants (removing up to 26,000 pounds of air pollutants per year)
                    - Reduce urban heat island effect (lowering ambient temperatures by 2-8°F through shade and evapotranspiration)
                    - Provide habitat for wildlife (supporting over 500 species in mature trees)
                    - Increase property values (boosting real estate value by 3-15%)
                    - Enhance mental well-being (reducing stress hormones by up to 16% and improving cognitive function)
                    - Lower noise pollution (absorbing up to 40% of urban sound)
                    - Create community gathering spaces (strengthening neighborhood bonds and identity)
                    - Reduce crime rates (areas with 10% more tree canopy report 12% less crime)
                    - Improve business performance (customers spend 9-12% more in tree-lined shopping districts)
                    - Extend pavement life (reducing thermal stress on street surfaces by up to 60%)
                    
                    ## Impact on Climate Change
                    
                    Urban trees play a crucial role in fighting climate change by:
                    1. Absorbing CO2 (a single mature tree can sequester 48 pounds annually)
                    2. Reducing energy consumption (cutting cooling costs by 15-35% and heating costs by 10-20%)
                    3. Preventing soil erosion (root networks stabilize up to 20 times their weight in soil)
                    4. Creating carbon sinks (U.S. urban forests store 700+ million tons of carbon)
                    5. Mitigating flash floods (reducing stormwater runoff by up to 35%)
                    6. Providing natural cooling (one mature tree provides cooling equivalent to 10 room-sized air conditioners)
                    7. Protecting biodiversity (supporting genetic diversity crucial for climate adaptation)
                    8. Filtering water pollutants (root systems remove up to 95% of certain contaminants)
                    9. Reducing urban ozone levels (leaf surfaces trap ozone precursors)
                    10. Serving as living laboratories (helping scientists track climate impacts in real-time)
                    
                    ## Economic Benefits of Urban Forestry
                    
                    Strategic tree planting delivers significant financial returns:
                    - Every ${'$'}1 invested in urban trees yields ${'$'}2.25 in benefits
                    - Trees near homes cut energy bills by ${'$'}100-250 annually
                    - Urban forests provide ${'$'}18.3 billion in annual pollution removal services
                    - Properly placed trees extend pavement life by 40-60%
                    - Hospital patients with tree views recover 8% faster, reducing healthcare costs
                    - Urban forestry creates 1 job per 1,000 trees in management and maintenance
                    
                    ## Challenges Facing Urban Trees
                    
                    Despite their benefits, urban trees face numerous threats:
                    1. Limited soil volume and quality
                    2. Pollution and salt exposure
                    3. Vandalism and mechanical damage
                    4. Pest and disease pressure
                    5. Climate change stressors
                    6. Competition with infrastructure
                    7. Inadequate municipal budgets
                    8. Inequitable distribution across neighborhoods
                    9. Poor species selection
                    10. Lack of maintenance expertise
                """.trimIndent(),
                "category" to ArticleCategory.ENVIRONMENTAL_BENEFITS.name,
                "imageUrl" to "https://www.kedevelopers.com/wp-content/uploads/2025/02/article3.jpg",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likes" to 0
            ),
            hashMapOf(
                "title" to "Best Practices for Tree Planting",
                "content" to """
                    # Tree Planting Guide
                   Follow these steps for successful tree planting:

                    ## Preparation
                    1. Choose the right location (consider mature tree size, proximity to structures, overhead wires, and sun exposure)
                    2. Check for underground utilities (call your local utility locating service at least 72 hours before digging)
                    3. Select appropriate species (native trees adapted to local climate require less maintenance and support local ecosystems)
                    4. Test soil drainage (dig a test hole and fill with water; if water remains after 24 hours, select a moisture-tolerant species)
                    5. Consider seasonal timing (early spring or fall planting typically yields best results in most climates)
                    6. Gather proper tools (shovel, garden hose, pruners, stake kit for larger specimens, and wheelbarrow)
                    7. Calculate sun exposure (map the area's light patterns throughout the day to ensure proper placement)
                    8. Research potential pest issues (select resistant varieties appropriate for your region)
                    
                    ## Planting Steps
                    1. Dig the proper size hole (2-3 times wider than the root ball but only as deep as the root ball itself)
                    2. Place the tree at the right depth (the root flare should be visible at soil level; planting too deep is a leading cause of tree failure)
                    3. Backfill with quality soil (use the original soil removed from the hole mixed with 10-20% compost; avoid excessive amendments)
                    4. Water thoroughly (create a soil basin around the tree and fill it slowly 2-3 times to eliminate air pockets)
                    5. Remove all tags, wires, and burlap (ensure no synthetic materials remain that could girdle the trunk or roots)
                    6. Stake only if necessary (in windy areas, use 2-3 stakes with loose, flexible ties; remove within one year)
                    7. Apply 2-4 inches of mulch (maintain a 3-inch gap between mulch and trunk to prevent rot and pest issues)
                    8. Prune only damaged branches (minimize initial pruning to reduce transplant shock)
                    9. Document planting date and species information (create a maintenance calendar and keep nursery information)
                    10. Take "before" photos (establish a visual record to track growth and development)
                    
                    ## Aftercare
                    - Regular watering (deep watering once or twice weekly for first two years is better than frequent shallow watering)
                    - Mulching (maintain a 3-4 foot diameter mulch ring, refreshing annually without creating "mulch volcanoes")
                    - Pruning when needed (focus on structure in years 2-5; avoid removing more than 25% of canopy in any year)
                    - Monitor for pests and diseases (inspect leaves, bark, and branches monthly during growing season)
                    - Fertilize sparingly (wait until second year, then use slow-release formulations if soil tests indicate deficiencies)
                    - Protect from wildlife (install trunk guards in areas with deer, rabbit, or rodent pressure)
                    - Remove stakes and ties (after first year or once tree can stand unsupported)
                    - Adjust watering with seasons (increase during drought, reduce during dormant periods)
                    - Schedule professional inspections (consider arborist visits every 3-5 years for structural assessment)
                    - Update documentation (record major events, treatments, and growth patterns for long-term care)
                    
                    ## Common Mistakes to Avoid
                    1. Planting too deep (the top of root ball should be slightly above surrounding soil)
                    2. Creating mulch volcanoes (never pile mulch against the trunk)
                    3. Over-fertilizing (less is more, especially for new plantings)
                    4. Improper staking (too tight or left on too long causes trunk damage)
                    5. Overwatering (soggy soil promotes root rot; allow soil to dry between waterings)
                    6. Underwatering during establishment (first two years are critical for root development)
                    7. Trunk damage from string trimmers (maintain mulch rings to eliminate need for mowing near trees)
                    8. Topping trees (never remove the central leader or main branches)
                    9. Planting too close to structures (consider mature width and potential root spread)
                    10. Ignoring site conditions (right tree, right place is the foundation of success)
                    
                    ## Long-Term Care Timeline
                    
                    ### Year 1
                    - Water deeply 1-2 times weekly (more during drought)
                    - Monitor for transplant shock
                    - Remove stakes after 6-12 months
                    - Maintain mulch ring
                    
                    ### Years 2-3
                    - Gradually reduce supplemental watering
                    - Begin structural pruning if needed
                    - Monitor for signs of establishment (new growth, normal leaf size)
                    - Apply fertilizer only if soil tests indicate deficiencies
                    
                    ### Years 4-10
                    - Establish regular pruning schedule
                    - Expand mulch ring as tree grows
                    - Monitor for encroaching utility lines
                    - Document growth rate and health indicators
                    
                    ### Years 10+
                    - Schedule professional inspections
                    - Prune to maintain clearance and structure
                    - Protect established root zone from construction
                    - Consider legacy planning for especially valuable specimens
                """.trimIndent(),
                "category" to ArticleCategory.HOW_TO_GUIDES.name,
                "imageUrl" to "https://www.kedevelopers.com/wp-content/uploads/2025/02/article2.jpeg",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likes" to 0
            ),
            hashMapOf(
                "title" to "Essential Tree Care Tips",
                "content" to """
                    # Caring for Your Trees
                    
                    ## Watering Guidelines
                    - New trees: Water deeply 2-3 times per week (delivering 10-15 gallons per watering session for most species)
                    - Established trees: Water during dry periods (apply approximately 10 gallons per inch of trunk diameter)
                    - Morning watering is best (reduces evaporation loss and fungal disease risk)
                    - Use slow-release methods (soaker hoses, drip irrigation, or tree watering bags for efficient absorption)
                    - Adjust for soil type (sandy soils require more frequent watering; clay soils need deeper, less frequent watering)
                    - Monitor soil moisture (insert a screwdriver 6-8 inches into soil; difficulty indicates watering is needed)
                    - Reduce frequency in cooler months (trees require less water during dormant periods)
                    - Water the entire root zone (extend watering area to the drip line of the canopy, not just near trunk)
                    - Consider rainfall amounts (subtract recent rainfall from watering schedule; 1 inch of rain equals about 625 gallons per 1,000 square feet)
                    - Watch for signs of overwatering (yellowing leaves, fungal growth, soggy soil) or under-watering (wilting, leaf scorch, premature leaf drop)
                    
                    ## Mulching
                    - Keep mulch 2-3 inches deep (replenish annually as it decomposes)
                    - Avoid volcano mulching (which promotes disease, pest issues, and trunk decay)
                    - Maintain mulch-free area near trunk (leave 3-6 inches of clear space around the base)
                    - Extend mulch to drip line when possible (covering the entire root zone offers maximum benefit)
                    - Use organic materials (wood chips, shredded bark, leaves, or pine needles break down to improve soil)
                    - Apply after soil warms in spring (mulch insulates, so applying too early keeps soil cooler longer)
                    - Refresh rather than replace (add only enough to maintain proper depth)
                    - Consider sheet mulching for new plantings (layering cardboard or newspaper under organic mulch suppresses weeds)
                    - Avoid plastic sheeting (which restricts oxygen and water movement)
                    - Incorporate compost when possible (mixing 25% compost with mulch accelerates soil improvement)
                    
                    ## Fertilization
                    - Test soil before fertilizing (many trees require no supplemental feeding)
                    - Use slow-release formulations (quick-release fertilizers can burn roots and create growth spurts)
                    - Apply in early spring or fall (avoid summer applications that promote tender growth before winter)
                    - Follow application rates precisely (more is not better; excess fertilizer damages trees and waterways)
                    - Focus on root zone (apply from near trunk to beyond drip line, where feeding roots are concentrated)
                    - Consider specific needs (fruit trees, evergreens, and ornamentals have different requirements)
                    - Use organic options when possible (compost tea, well-rotted manure, and fish emulsion improve soil health)
                    - Avoid fertilizing stressed trees (wait until recovery is evident before feeding)
                    - Reduce rates for mature trees (they require less supplemental nutrition than younger specimens)
                    - Remember that mulch provides natural nutrition (properly mulched trees often need minimal fertilization)
                    
                    ## Pruning Basics
                    - Prune during dormancy for most species (late winter/early spring before new growth emerges)
                    - Remove the three D's first (dead, damaged, and diseased branches should be priority cuts)
                    - Maintain proper branch collars (never make flush cuts against the trunk)
                    - Limit removal to 25% of canopy maximum (excessive pruning stresses trees)
                    - Use sharp, clean tools (disinfect between trees and major cuts with 70% isopropyl alcohol)
                    - Make proper cuts (just outside branch collar at a slight angle to shed water)
                    - Address competing leaders early (select a single dominant leader in young trees)
                    - Avoid topping trees (this destructive practice creates hazardous regrowth)
                    - Consider hiring certified arborists for large trees (professional pruning is an investment in tree health)
                    - Follow species-specific guidelines (flowering trees, evergreens, and fruit trees have unique timing requirements)
                    
                    ## Common Problems
                    - Watch for pest infestations (common signs include chewed leaves, sticky residue, webbing, or visible insects)
                    - Monitor for disease symptoms (spots, blotches, powdery coating, cankers, or unusual growths)
                    - Check for structural issues (cracks, leaning, multiple trunks with weak attachments, or exposed roots)
                    - Be alert for girdling roots (roots that wrap around the trunk, visible at the soil line)
                    - Inspect for trunk damage (mechanical injuries from mowers or trimmers are entry points for pathogens)
                    - Look for leaf abnormalities (discoloration, early drop, stunted size, or unusual patterns)
                    - Monitor growth rate changes (significant slowing may indicate stress or root problems)
                    - Watch the canopy for dieback (starting at branch tips and progressing inward)
                    - Check for mushrooms or conks (signs of potential root or trunk decay)
                    - Document treatment approaches (keep records of interventions and results)
                    
                    ## Seasonal Care Calendar
                    
                    ### Spring
                    - Inspect for winter damage
                    - Apply fresh mulch after soil warms
                    - Prune spring-flowering trees after blooms fade
                    - Monitor for early-season pests like aphids and scale
                    - Begin regular watering schedule for newly planted trees
                    
                    ### Summer
                    - Increase watering during drought periods
                    - Check for signs of heat stress
                    - Monitor for Japanese beetles and other seasonal pests
                    - Avoid major pruning (which can stress trees)
                    - Protect trunks from sunscald in hot climates
                    
                    ### Fall
                    - Reduce watering as temperatures cool
                    - Avoid fertilizing (which promotes tender growth before winter)
                    - Remove fallen leaves from lawn (but consider leaving them under trees as natural mulch)
                    - Plant new trees early enough to establish before winter
                    - Install guards for protection from wildlife and winter sun
                    
                    ### Winter
                    - Prune deciduous trees during dormancy
                    - Water during dry, thawed periods
                    - Inspect for snow and ice damage after storms
                    - Plan spring treatments for identified issues
                    - Protect sensitive species from extreme cold and desiccating winds
                    
                    ## Advanced Tree Health Strategies
                    - Schedule professional inspections (every 3-5 years for valuable specimens)
                    - Consider soil aeration for compacted sites (using an air spade or vertical mulching)
                    - Implement integrated pest management (IPM) approaches (focusing on prevention and least-toxic interventions)
                    - Install lightning protection for significant trees (especially those near structures)
                    - Establish proper irrigation systems (particularly for drought-prone regions)
                    - Apply growth regulators when appropriate (to minimize utility line conflicts or reduce pruning needs)
                    - Protect trees during construction (fence off root zones at 1.5 times the drip line)
                    - Address soil pH issues when symptoms appear (yellowing leaves often indicate alkaline-induced chlorosis)
                    - Consider cabling or bracing for valuable trees with structural defects (performed by certified arborists only)
                    - Develop succession planting plans (gradually replacing aging trees while maintaining canopy coverage)
                """.trimIndent(),
                "category" to ArticleCategory.CARE_TIPS.name,
                "imageUrl" to "https://www.kedevelopers.com/wp-content/uploads/2025/02/article1.webp",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "likes" to 0
            )
        )

        sampleArticles.forEach { article ->
            try {
                articlesCollection.add(article).await()
            } catch (e: Exception) {
                println("Error adding article: ${e.message}")
            }
        }
    }
} 