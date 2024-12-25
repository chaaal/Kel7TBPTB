const Info  = require('../models/Info');
const KPRequest = require('../models/KPRequest');
const GroupMember = require('../models/GroupMember');
const Reply  = require('../models/Reply');
const Group = require('../models/Group');
const Member = require('../models/Member');




const fs = require('fs');
const path = require('path');

// Info Controllers
const getInfos = async (req, res) => {
  try {
    const infos = await Info.findAll({
      order: [['createdAt', 'DESC']]
    });

    return res.status(200).json({
      success: true,
      data: infos
    });
  } catch (error) {
    console.error('Error fetching infos:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch information',
      error: error.message
    });
  }
};

const getInfo = async (req, res) => {
  try {
    const { id } = req.params;
    
    const info = await Info.findByPk(id);
    
    if (!info) {
      return res.status(404).json({
        success: false,
        message: 'Information not found'
      });
    }

    return res.status(200).json({
      success: true,
      data: info
    });
  } catch (error) {
    console.error('Error fetching info:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch information',
      error: error.message
    });
  }
};

const addInfo = async (req, res) => {
  try {
    const { title, description } = req.body;

    // Basic validation
    if (!title || !description) {
      return res.status(400).json({
        success: false,
        message: 'Title and description are required'
      });
    }

    const newInfo = await Info.create({
      title,
      description
    });

    return res.status(201).json({
      success: true,
      message: 'Information added successfully',
      data: newInfo
    });
  } catch (error) {
    console.error('Error adding info:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to add information',
      error: error.message
    });
  }
};

// Archive Controllers
const getArchives = async (req, res) => {
  try {
    const archives = await Reply.findAll({
      include: [
        {
          model: KPRequest,
          include: [
            {
              model: Group,
            }
          ]
        }
      ],
      order: [['createdAt', 'DESC']] // Tambahkan pengurutan jika diperlukan
    });

    return res.status(200).json({
      success: true,
      data: archives
    });
  } catch (error) {
    console.error('Error fetching archives:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch archives',
      error: error.message
    });
  }
};


const downloadProposal = async (req, res) => {
  try {
    const { id } = req.params;
    
    const archive = await KPRequest.findOne({
      where: {
        id,
        status: 'Approved'
      }
    });
    
    if (!archive || !archive.proposalUrl) {
      return res.status(404).json({
        success: false,
        message: archive.proposalUrl
      });
    }

    const filePath = path.join(__dirname, '..', 'storage', 'proposals', archive.proposalUrl);
    
    if (!fs.existsSync(filePath)) {
      return res.status(404).json({
        success: false,
        message: archive.proposalUrl
      });
    }

    const fileName = `KP_Proposal_${archive.groupName}_${id}.pdf`;
    res.download(filePath, fileName, (err) => {
      if (err) {
        console.error('Error downloading proposal:', err);
        return res.status(500).json({
          success: false,
          message: 'Error downloading proposal',
          error: err.message
        });
      }
    });
  } catch (error) {
    console.error('Error downloading proposal:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to download proposal',
      error: error.message
    });
  }
};

const downloadReply = async (req, res) => {
  try {
    const { id } = req.params;
    
    const reply = await Reply.findOne({
      where: { idKP: id },
      include: [{
        model: KPRequest,
        attributes: ['groupName', 'status'],
        where: { status: 'Approved' }
      }]
    });
    
    if (!reply || !reply.responseLetterUrl) {
      return res.status(404).json({
        success: false,
        message: 'Reply letter not found'
      });
    }

    const filePath = path.join(__dirname, '..', 'storage', 'replies', reply.responseLetterUrl);
    
    if (!fs.existsSync(filePath)) {
      return res.status(404).json({
        success: false,
        message: 'Reply letter file not found on server'
      });
    }

    const fileName = `KP_Reply_${reply.KPRequest.groupName}_${id}.pdf`;
    res.download(filePath, fileName, (err) => {
      if (err) {
        console.error('Error downloading reply:', err);
        return res.status(500).json({
          success: false,
          message: 'Error downloading reply letter',
          error: err.message
        });
      }
    });
  } catch (error) {
    console.error('Error downloading reply:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to download reply letter',
      error: error.message
    });
  }
};

module.exports = {
  getInfos,
  getInfo,
  addInfo,
  getArchives,
  downloadProposal,
  downloadReply
};