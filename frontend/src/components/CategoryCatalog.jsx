import { useEffect, useState } from 'react';
import {
  activateCategory,
  createCategory,
  deactivateCategory,
  listCategories,
  updateCategory,
} from '../api.js';
import { CloseIcon, LayersIcon, PlusIcon, SearchIcon } from './Icons.jsx';

const EMPTY_FORM = {
  code: '',
  name: '',
  description: '',
};

const STATUS_OPTIONS = [
  { value: 'ALL', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
];

export default function CategoryCatalog({ onCategoriesChanged }) {
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [busyId, setBusyId] = useState(null);

  async function loadCategories(currentSearch = search, currentStatus = statusFilter) {
    setLoading(true);
    setError('');
    try {
      const data = await listCategories({ search: currentSearch, status: currentStatus });
      setCategories(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = setTimeout(() => {
      loadCategories(search, statusFilter);
    }, 180);
    return () => clearTimeout(timer);
  }, [search, statusFilter]);

  useEffect(() => {
    if (!formOpen) return undefined;
    function onKeyDown(event) {
      if (event.key === 'Escape' && !submitting) {
        closeForm();
      }
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [formOpen, submitting]);

  function openCreateForm() {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setError('');
    setNotice('');
    setFormOpen(true);
  }

  function openEditForm(category) {
    setEditingId(category.id);
    setForm({
      code: category.code,
      name: category.name,
      description: category.description || '',
    });
    setError('');
    setNotice('');
    setFormOpen(true);
  }

  function closeForm() {
    setFormOpen(false);
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setNotice('');

    const payload = {
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      description: form.description.trim(),
    };

    try {
      if (editingId) {
        await updateCategory(editingId, payload);
        setNotice(`Updated category "${payload.name}".`);
      } else {
        await createCategory(payload);
        setNotice(`Created category "${payload.name}".`);
      }
      closeForm();
      await loadCategories();
      await onCategoriesChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleToggleActive(category) {
    setBusyId(category.id);
    setError('');
    setNotice('');
    try {
      if (category.active) {
        await deactivateCategory(category.id);
        setNotice(`Deactivated category "${category.name}".`);
      } else {
        await activateCategory(category.id);
        setNotice(`Reactivated category "${category.name}".`);
      }
      await loadCategories();
      await onCategoriesChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  }

  const hasFilters = Boolean(search.trim()) || statusFilter !== 'ALL';

  return (
    <section className="panel" aria-labelledby="categories-heading">
      <div className="panel-header">
        <div>
          <h2 id="categories-heading">Category Catalog</h2>
          <p>Organize parts into families and soft-deactivate categories no longer in use.</p>
        </div>
        <button type="button" onClick={openCreateForm}>
          <PlusIcon />
          <span>New category</span>
        </button>
      </div>

      <div className="panel-body">
        <div className="toolbar">
          <div className="search-box">
            <span className="search-icon"><SearchIcon /></span>
            <label className="sr-only" htmlFor="category-search">Search categories</label>
            <input
              id="category-search"
              type="search"
              value={search}
              onChange={event => setSearch(event.target.value)}
              placeholder="Search by code, name, or description…"
            />
            {search && (
              <button
                type="button"
                className="search-clear"
                onClick={() => setSearch('')}
                aria-label="Clear search"
              >
                <CloseIcon />
              </button>
            )}
          </div>

          <div className="segmented" role="group" aria-label="Filter categories by status">
            {STATUS_OPTIONS.map(option => (
              <button
                key={option.value}
                type="button"
                className={statusFilter === option.value ? 'is-selected' : ''}
                onClick={() => setStatusFilter(option.value)}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {error && !formOpen && (
          <div role="alert" className="alert error">
            <span>{error}</span>
            <button type="button" className="alert-dismiss" onClick={() => setError('')} aria-label="Dismiss error">
              <CloseIcon />
            </button>
          </div>
        )}
        {notice && (
          <div role="status" className="alert success">
            <span>{notice}</span>
            <button type="button" className="alert-dismiss" onClick={() => setNotice('')} aria-label="Dismiss notice">
              <CloseIcon />
            </button>
          </div>
        )}

        {loading ? (
          <div className="empty-card">
            <p>Loading categories…</p>
          </div>
        ) : categories.length === 0 ? (
          <div className="empty-card">
            <div className="empty-icon"><LayersIcon /></div>
            <h3>{hasFilters ? 'No matching categories' : 'No categories yet'}</h3>
            <p>
              {hasFilters
                ? 'Try clearing your search or switching the status filter.'
                : 'Create your first electronics category (for example, Resistors, Capacitors, or Microcontrollers).'}
            </p>
            {hasFilters ? (
              <button
                type="button"
                className="secondary"
                onClick={() => {
                  setSearch('');
                  setStatusFilter('ALL');
                }}
              >
                Reset filters
              </button>
            ) : (
              <button type="button" onClick={openCreateForm}>
                <PlusIcon />
                <span>Create category</span>
              </button>
            )}
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Category</th>
                  <th>Assigned Parts</th>
                  <th>Status</th>
                  <th className="actions-col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {categories.map(category => (
                  <tr key={category.id} className={!category.active ? 'row-inactive' : ''}>
                    <td>
                      <span className="code-pill">{category.code}</span>
                    </td>
                    <td>
                      <div className="primary-cell">{category.name}</div>
                      {category.description && <div className="sub-cell">{category.description}</div>}
                    </td>
                    <td>
                      <span className="count-text">
                        <strong>{category.activeComponentCount}</strong> active / {category.componentCount} total
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${category.active ? 'badge-active' : 'badge-inactive'}`}>
                        {category.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="actions-col">
                      <div className="row-actions">
                        <button
                          type="button"
                          className="secondary small"
                          onClick={() => openEditForm(category)}
                          disabled={busyId === category.id}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className={`${category.active ? 'danger-soft' : 'secondary'} small`}
                          onClick={() => handleToggleActive(category)}
                          disabled={busyId === category.id}
                        >
                          {busyId === category.id
                            ? 'Updating…'
                            : category.active
                              ? 'Deactivate'
                              : 'Activate'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {formOpen && (
        <div
          className="modal-backdrop"
          onClick={event => {
            if (event.target === event.currentTarget && !submitting) {
              closeForm();
            }
          }}
        >
          <div className="modal-card modal-compact" role="dialog" aria-modal="true" aria-labelledby="cat-modal-title">
            <div className="modal-header">
              <div>
                <h3 id="cat-modal-title">{editingId ? 'Edit Category' : 'New Category'}</h3>
                <p className="modal-subtitle">
                  {editingId ? 'Update category code, name, or description.' : 'Add a new component family.'}
                </p>
              </div>
              <button
                type="button"
                className="ghost icon-btn"
                onClick={closeForm}
                disabled={submitting}
                aria-label="Close modal"
              >
                <CloseIcon />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && (
                  <div role="alert" className="alert error">
                    <span>{error}</span>
                  </div>
                )}

                <div className="form-grid">
                  <label>
                    <span>Code *</span>
                    <input
                      type="text"
                      required
                      maxLength={24}
                      value={form.code}
                      onChange={event => setForm(prev => ({ ...prev, code: event.target.value }))}
                      placeholder="e.g. RES"
                      autoFocus
                    />
                  </label>
                  <label>
                    <span>Name *</span>
                    <input
                      type="text"
                      required
                      maxLength={80}
                      value={form.name}
                      onChange={event => setForm(prev => ({ ...prev, name: event.target.value }))}
                      placeholder="e.g. Resistors"
                    />
                  </label>
                  <label className="full-span">
                    <span>Description</span>
                    <textarea
                      rows={3}
                      maxLength={300}
                      value={form.description}
                      onChange={event => setForm(prev => ({ ...prev, description: event.target.value }))}
                      placeholder="Optional notes on the parts stored in this category"
                    />
                  </label>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="secondary" onClick={closeForm} disabled={submitting}>
                  Cancel
                </button>
                <button type="submit" disabled={submitting}>
                  {submitting ? 'Saving…' : editingId ? 'Save changes' : 'Create category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
