package com.example.ui.editor

data class UnsplashPhotoItem(
    val id: String,
    val title: String,
    val category: String,
    val drawableResName: String? = null,
    val imageUrl: String,
    val photographer: String
)

object UnsplashAssets {
    val curatedPhotos = listOf(
        UnsplashPhotoItem(
            id = "photo_workspace",
            title = "Modern Creative Studio",
            category = "Workspace",
            drawableResName = "splash_hero_1791399236200",
            imageUrl = "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80",
            photographer = "Studio Nordic"
        ),
        UnsplashPhotoItem(
            id = "photo_logo_badge",
            title = "Corporate Badge Logo",
            category = "Logos",
            drawableResName = "pdf_icon_fg_1791399220170",
            imageUrl = "https://images.unsplash.com/photo-1572044162444-ad60f128bdea?auto=format&fit=crop&w=600&q=80",
            photographer = "Brandcraft"
        ),
        UnsplashPhotoItem(
            id = "photo_architecture",
            title = "Geometric Architecture",
            category = "Architecture",
            imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=600&q=80",
            photographer = "Daniel Von Appen"
        ),
        UnsplashPhotoItem(
            id = "photo_coffee_contract",
            title = "Document & Stationery",
            category = "Business",
            imageUrl = "https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=600&q=80",
            photographer = "Scott Graham"
        ),
        UnsplashPhotoItem(
            id = "photo_meeting",
            title = "Executive Collaboration",
            category = "Business",
            imageUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=600&q=80",
            photographer = "Annie Spratt"
        ),
        UnsplashPhotoItem(
            id = "photo_nature_minimal",
            title = "Calm Horizon",
            category = "Nature",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80",
            photographer = "Sean Oulashin"
        ),
        UnsplashPhotoItem(
            id = "photo_tech_code",
            title = "Software Engineering",
            category = "Technology",
            imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=600&q=80",
            photographer = "Fatos Bytyqi"
        ),
        UnsplashPhotoItem(
            id = "photo_abstract_mesh",
            title = "Gradient Texture Pattern",
            category = "Abstract",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80",
            photographer = "Milad Fakurian"
        )
    )
}
