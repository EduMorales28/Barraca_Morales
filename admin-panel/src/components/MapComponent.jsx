import { MapPin } from 'lucide-react'

export default function MapComponent({ direccion, coordenadas = { lat: -34.6037, lng: -58.3816 } }) {
  return (
    <div className="w-full h-96 bg-gray-200 rounded-lg flex items-center justify-center border border-gray-300 relative overflow-hidden">
      {/* Placeholder for Google Maps */}
      <div className="absolute inset-0 bg-gradient-to-br from-gray-100 to-gray-200" />
      
      <div className="relative z-10 text-center">
        <div className="bg-white rounded-lg shadow-lg p-6 max-w-xs">
          <div className="flex justify-center mb-3">
            <MapPin size={32} className="text-primary" />
          </div>
          <p className="text-sm text-gray-600 mb-2">
            <strong>Ubicación:</strong>
          </p>
          <p className="text-sm font-medium text-gray-900 mb-3">{direccion}</p>
          <p className="text-xs text-gray-500">
            Lat: {coordenadas.lat}, Lng: {coordenadas.lng}
          </p>
          <button className="mt-4 w-full px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-orange-700 transition">
            Ver en Google Maps
          </button>
        </div>
      </div>

      {/* Note */}
      <div className="absolute bottom-4 right-4 bg-blue-100 text-blue-800 text-xs px-3 py-2 rounded-lg z-20">
        Próximamente: Integración con Google Maps
      </div>
    </div>
  )
}
