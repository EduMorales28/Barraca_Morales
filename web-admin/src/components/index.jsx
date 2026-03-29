export function StateBadge({ estado }) {
  const config = {
    pendiente: { bg: 'bg-yellow-100', text: 'text-yellow-800', dot: 'bg-yellow-500' },
    asignado: { bg: 'bg-blue-100', text: 'text-blue-800', dot: 'bg-blue-500' },
    aceptado: { bg: 'bg-emerald-100', text: 'text-emerald-800', dot: 'bg-emerald-500' },
    entregado: { bg: 'bg-green-100', text: 'text-green-800', dot: 'bg-green-500' }
  }

  const style = config[estado] || config.pendiente

  return (
    <span className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-semibold ${style.bg} ${style.text}`}>
      <span className={`w-2 h-2 rounded-full ${style.dot}`}></span>
      {estado}
    </span>
  )
}

export function PedidoCard({ pedido, onView, onAsignar, onAceptar, canAssign, canAccept }) {
  const mapQuery = encodeURIComponent(pedido.direccion || '')

  return (
    <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow border-l-4 border-blue-500">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-lg font-bold text-gray-800">{pedido.cliente}</h3>
          <p className="text-sm text-gray-600">{pedido.direccion}</p>
        </div>
        <StateBadge estado={pedido.estado} />
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
        <div>
          <p className="text-gray-600">Items</p>
          <p className="font-semibold">{pedido.items?.length || 0}</p>
        </div>
        <div>
          <p className="text-gray-600">Conductor</p>
          <p className="font-semibold">{pedido.conductor_nombre || '-'}</p>
        </div>
      </div>

      <div className="mb-4 overflow-hidden rounded-lg border border-gray-200 bg-gray-50">
        {pedido.direccion ? (
          <>
            <iframe
              title={`Mapa de ${pedido.cliente}`}
              src={`https://www.google.com/maps?q=${mapQuery}&output=embed`}
              className="h-44 w-full border-0"
              loading="lazy"
              referrerPolicy="no-referrer-when-downgrade"
            ></iframe>
            <div className="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 text-sm">
              <span className="text-gray-500">Vista previa de ubicación</span>
              <a
                href={`https://www.google.com/maps/search/?api=1&query=${mapQuery}`}
                target="_blank"
                rel="noreferrer"
                className="font-semibold text-blue-600 hover:text-blue-700"
              >
                Abrir mapa
              </a>
            </div>
          </>
        ) : (
          <div className="flex h-32 items-center justify-center text-sm text-gray-500">
            Sin dirección cargada
          </div>
        )}
      </div>

      <div className="flex gap-2">
        <button
          onClick={() => onView(pedido)}
          className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
        >
          Ver Detalles
        </button>
        {canAssign && pedido.estado === 'pendiente' && (
          <button
            onClick={() => onAsignar(pedido)}
            className="flex-1 bg-green-600 hover:bg-green-700 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
          >
            Asignar
          </button>
        )}
        {canAccept && pedido.estado === 'asignado' && (
          <button
            onClick={() => onAceptar(pedido)}
            className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
          >
            Aceptar
          </button>
        )}
      </div>
    </div>
  )
}

export function StatCard({ title, value, icon, color }) {
  return (
    <div className={`bg-white rounded-lg shadow-md p-6 border-t-4 ${color}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-gray-600 text-sm font-semibold">{title}</p>
          <p className="text-3xl font-bold text-gray-800 mt-2">{value}</p>
        </div>
        <span className="text-4xl opacity-20">{icon}</span>
      </div>
    </div>
  )
}

export function FormInput({ label, type = 'text', value, onChange, required, placeholder, disabled }) {
  return (
    <div className="space-y-2">
      <label className={`block text-sm font-semibold ${disabled ? 'text-gray-400' : 'text-gray-700'}`}>{label}</label>
      <input
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        disabled={disabled}
        className={`w-full px-4 py-2 border-2 rounded-lg transition-colors ${
          disabled
            ? 'border-gray-100 bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'border-gray-200 focus:outline-none focus:border-blue-500'
        }`}
      />
    </div>
  )
}

export function FormSelect({ label, value, onChange, options, required }) {
  return (
    <div className="space-y-2">
      <label className="block text-sm font-semibold text-gray-700">{label}</label>
      <select
        value={value}
        onChange={onChange}
        required={required}
        className="w-full px-4 py-2 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-blue-500 transition-colors"
      >
        <option value="">Seleccionar...</option>
        {options.map((opt) => (
          <option key={opt.id} value={opt.id}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  )
}

export function Button({ children, type = 'button', variant = 'primary', onClick, disabled, fullWidth }) {
  const baseStyles = 'font-bold py-2 px-6 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed'
  const variants = {
    primary: 'bg-blue-600 hover:bg-blue-700 text-white',
    secondary: 'bg-gray-400 hover:bg-gray-500 text-white',
    danger: 'bg-red-600 hover:bg-red-700 text-white',
    success: 'bg-green-600 hover:bg-green-700 text-white'
  }

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`${baseStyles} ${variants[variant]} ${fullWidth ? 'w-full' : ''}`}
    >
      {children}
    </button>
  )
}