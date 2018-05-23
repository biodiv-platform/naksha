package com.strandls.naksha.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.strandls.naksha.dao.DAOException;
import com.strandls.naksha.dao.DAOUtil;
import com.strandls.naksha.dao.LayerDAO;
import com.strandls.naksha.layers.models.LayerAttributes;

/**
 * Implementation of {@link LayerDAO}
 * 
 * @author mukund
 *
 */
public class LayerDAOJDBC implements LayerDAO {

	@Inject
	Connection connection;

	private static final String GET_ATTRIBUTES = "SELECT c.column_name, pgd.description\n"
			+ "FROM pg_catalog.pg_statio_all_tables as st\n"
			+ "INNER JOIN pg_catalog.pg_description pgd on (pgd.objoid=st.relid)\n"
			+ "RIGHT OUTER JOIN information_schema.columns c on (pgd.objsubid=c.ordinal_position and  c.table_schema=st.schemaname and c.table_name=st.relname)\n"
			+ "WHERE table_schema = 'public' and table_name = ?";

	private static final String GET_LAYERNAME_WITH_TAG = "SELECT layer_name, tags FROM \"Meta_Layer\"";

	@Override
	public List<LayerAttributes> getLayerAttributes(String layerName) {
		List<LayerAttributes> layerAttributes = new ArrayList<>();

		try (PreparedStatement statement = DAOUtil.prepareStatement(connection, GET_ATTRIBUTES, false, layerName);
				ResultSet resultSet = statement.executeQuery();) {
			while (resultSet.next()) {
				String name = resultSet.getString("column_name");
				String desc = resultSet.getString("description");
				layerAttributes.add(new LayerAttributes(name, desc));
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}

		return layerAttributes;
	}

	@Override
	public List<String> getLayerNamesWithTag(String tag) {
		List<String> layerNames = new ArrayList<>();

		try (PreparedStatement statement = connection.prepareStatement(GET_LAYERNAME_WITH_TAG);
				ResultSet resultSet = statement.executeQuery();) {
			while (resultSet.next()) {
				String tags = resultSet.getString("tags");
				if (tags != null)	
					for (String t : tags.split(",")) {
						if (t.trim().toLowerCase().contains(tag.toLowerCase()))
							layerNames.add(resultSet.getString("layer_name"));
					}
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}

		return layerNames;
	}

}
